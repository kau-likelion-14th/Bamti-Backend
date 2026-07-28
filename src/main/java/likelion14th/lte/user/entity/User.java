package likelion14th.lte.user.entity;

import jakarta.persistence.*;
import likelion14th.lte.Entity.BaseEntity;
import likelion14th.lte.follow.entity.Follow;
import likelion14th.lte.statistic.entity.Statistic;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(length = 16, nullable = false, unique = true)
    private String userTag;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    @Column(columnDefinition = "TEXT")
    private String s3ImageKey;

    // 나를 팔로우하는 사람들 (toUser = 나)
    @OneToMany(mappedBy = "toUser")
    private List<Follow> followers = new ArrayList<>();

    // 내가 팔로우하는 사람들 (fromUser = 나)
    @OneToMany(mappedBy = "fromUser")
    private List<Follow> followings = new ArrayList<>();

    // User 생성 시 Statistic 자동 생성 (CascadeType.ALL로 함께 저장)
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "statistic_id")
    private Statistic statistic;

    @Builder(access = AccessLevel.PUBLIC)
    private User(String username, String userTag, String introduction) {
        this.username = username;
        this.userTag = userTag;
        this.introduction = introduction;
        this.statistic = Statistic.create();
    }

    public void updateIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
