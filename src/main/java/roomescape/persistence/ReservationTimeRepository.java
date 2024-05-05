package roomescape.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.service.response.AvailableReservationTimeResponse;

@Repository
public class ReservationTimeRepository {

    private static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER =
            (resultSet, rowNum) -> new ReservationTime(
                    resultSet.getLong("id"),
                    LocalTime.parse(resultSet.getString("start_at"))
            );
    private static final RowMapper<AvailableReservationTimeResponse> AVAILABLE_RESERVATION_TIME_RESPONSE_ROW_MAPPER =
            (resultSet, rowNum) -> new AvailableReservationTimeResponse(
                    resultSet.getLong("id"),
                    resultSet.getString("start_at"),
                    resultSet.getBoolean("already_booked")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationTimeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationTime create(ReservationTime reservationTime) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", reservationTime.getStartAt());
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return new ReservationTime(id, reservationTime.getStartAt());
    }

    public void removeById(Long id) {
        jdbcTemplate.update("DELETE FROM reservation_time WHERE id = ?", id);
    }

    public boolean hasDuplicateTime(ReservationTime reservationTime) {
        String sql = """
                SELECT count(*) 
                FROM reservation_time 
                WHERE start_at = ?
                """;

        int hasCount = jdbcTemplate.queryForObject(sql, Integer.class, reservationTime.getStartAt());

        return hasCount > 0;
    }

    public List<ReservationTime> findAll() {
        return jdbcTemplate.query("SELECT id, start_at FROM reservation_time", RESERVATION_TIME_ROW_MAPPER);
    }

    public Optional<ReservationTime> findById(Long id) {
        String sql = "SELECT id, start_at FROM reservation_time WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, RESERVATION_TIME_ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<AvailableReservationTimeResponse> findAvailableReservationTimes(LocalDate date, long themeId) {
        String sql = """
                        SELECT
                            t.id,
                            t.start_at,
                            (
                             CASE r.id
                               WHEN is null THEN false
                               ELSE true
                             END
                             ) AS already_booked
                        FROM reservation_time AS t
                        LEFT JOIN reservation AS r
                        ON t.id = r.time_id
                        AND r.date = ?
                        AND r.theme_id = ?
                        ORDER BY convert(t.start_at, TIME) ASC;
                """;

        return jdbcTemplate.query(sql, AVAILABLE_RESERVATION_TIME_RESPONSE_ROW_MAPPER, date, themeId);
    }
}
