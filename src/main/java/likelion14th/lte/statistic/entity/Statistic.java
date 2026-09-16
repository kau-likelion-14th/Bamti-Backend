package likelion14th.lte.statistic.entity;

import jakarta.persistence.*;
import likelion14th.lte.todo.entity.WeekEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Getter
@Table(name = "statistic")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Statistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int streak;

    private int monthPercent;

    @OneToMany(mappedBy = "statistic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatWeek> statWeeks = new ArrayList<>();

    // 정적 팩토리 메서드 - 외부에서는 이 메서드로만 생성 가능
    public static Statistic create() {
        Statistic statistic = new Statistic();
        statistic.streak = 0;
        statistic.monthPercent = 0;
        statistic.initializeWeeks();
        return statistic;
    }

    // 요일 7개 초기화 (캡슐화)
    private void initializeWeeks() {
        for (WeekEnum week : WeekEnum.values()) {
            StatWeek statWeek = StatWeek.create(week, this);
            this.statWeeks.add(statWeek);
        }
    }

    // 가장 투두 많이 완료한 요일
    public WeekEnum getMostTodoWeek() {
        return statWeeks.stream()
                .max(Comparator.comparingInt(StatWeek::getCount))
                .map(StatWeek::getWeek)
                .orElse(null);
    }

    // streak 갱신 (도메인 로직 캡슐화)
    public void increaseStreakIfSuccess(boolean isSuccess) {
        if (isSuccess) {
            this.streak++;
        } else {
            this.streak = 0;
        }
    }

    // 완료율 갱신
    public void updateMonthPercent(int monthPercent) {
        this.monthPercent = monthPercent;
    }

    // 요일별 카운트 증가
    public void increaseWeekCount(DayOfWeek dayOfWeek) {
        statWeeks.stream()
                .filter(w -> w.getWeek().toDayOfWeek() == dayOfWeek)
                .findFirst()
                .ifPresent(StatWeek::increaseCount);
    }
}
