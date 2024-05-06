package roomescape.service;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.persistence.ReservationRepository;
import roomescape.persistence.ReservationTimeRepository;
import roomescape.persistence.ThemeRepository;
import roomescape.service.request.ReservationRequest;
import roomescape.service.response.ReservationResponse;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public ReservationResponse createReservation(ReservationRequest createDto) {
        ReservationTime reservationTime = getReservationTime(createDto.timeId());
        Theme theme = getTheme(createDto.themeId());
        Reservation reservation = createDto.toDomain(reservationTime, theme);
        reservation.validateDateTime();
        validateDuplicateReservation(reservation);

        Reservation createdReservation = reservationRepository.create(reservation);
        return ReservationResponse.from(createdReservation);
    }

    private ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당되는 예약 시간이 없습니다."));
    }

    private Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당되는 테마가 없습니다."));
    }

    private void validateDuplicateReservation(Reservation reservation) {
        if (reservationRepository.hasDuplicateReservation(reservation)) {
            throw new IllegalStateException("중복된 예약이 존재합니다.");
        }
    }

    public void deleteReservation(Long id) {
        reservationRepository.removeById(id);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
