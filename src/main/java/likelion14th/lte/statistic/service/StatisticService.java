package likelion14th.lte.statistic.service;

import jakarta.persistence.EntityManager;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.statistic.dto.StatisticResponse;
import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.todo.repository.TodoDateRepository;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StatisticService {

    private final UserRepository userRepository;
    private final TodoDateRepository todoDateRepository;
    private final EntityManager entityManager;

    // 과제 1: 통계 조회
    @Transactional(readOnly = true)
    public StatisticResponse getStatistic(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
        return StatisticResponse.from(user.getStatistic());
    }

    // 과제 2: 단일 유저 통계 갱신 (내부 메서드)
    private void updateStatistic(User user) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Statistic statistic = user.getStatistic();

        // 어제 완료/미완료 여부 확인
        boolean hasCompleted = todoDateRepository.existsByTodo_User_IdAndDateAndCompleted(
                user.getId(), yesterday, true);
        boolean hasFailed = todoDateRepository.existsByTodo_User_IdAndDateAndCompleted(
                user.getId(), yesterday, false);

        boolean isSuccess = hasCompleted && !hasFailed;

        // streak 갱신
        statistic.increaseStreakIfSuccess(isSuccess);

        // 성공한 날이면 요일 카운트 증가
        if (isSuccess) {
            statistic.increaseWeekCount(yesterday.getDayOfWeek());
        }

        // 최근 30일 완료율 갱신
        LocalDate thirtyDaysAgo = yesterday.minusDays(30);
        long completedCount = todoDateRepository.countByTodo_User_IdAndDateBetweenAndCompleted(
                user.getId(), thirtyDaysAgo, yesterday, true);
        long failedCount = todoDateRepository.countByTodo_User_IdAndDateBetweenAndCompleted(
                user.getId(), thirtyDaysAgo, yesterday, false);
        long totalCount = completedCount + failedCount;

        int monthPercent = totalCount == 0 ? 0 : (int) (completedCount * 100 / totalCount);
        statistic.updateMonthPercent(monthPercent);
    }

    // 과제 3: 전체 유저 배치 갱신
    @Transactional
    public void updateAllStatistics() {
        int page = 0;
        int size = 500;
        Page<User> userPage;

        do {
            userPage = userRepository.findAll(PageRequest.of(page, size));
            for (User user : userPage.getContent()) {
                updateStatistic(user);
            }
            entityManager.flush();   // 변경 내용 DB에 반영
            entityManager.clear();   // 영속성 컨텍스트 비워 메모리 관리
            page++;
        } while (userPage.hasNext());
    }
}
