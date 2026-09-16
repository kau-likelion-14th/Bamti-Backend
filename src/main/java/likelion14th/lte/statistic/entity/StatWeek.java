package likelion14th.lte.statistic.entity;

import jakarta.persistence.*;
import likelion14th.lte.todo.entity.WeekEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "stat_week")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StatWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private WeekEnum week;

    private int count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statistic_id")
    private Statistic statistic;

    public static StatWeek create(WeekEnum week, Statistic statistic) {
        StatWeek statWeek = new StatWeek();
        statWeek.week = week;
        statWeek.count = 0;
        statWeek.statistic = statistic;
        return statWeek;
    }

    public void increaseCount() {
        this.count++;
    }
}
