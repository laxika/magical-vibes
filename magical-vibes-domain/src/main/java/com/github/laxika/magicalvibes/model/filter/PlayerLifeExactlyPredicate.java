package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/**
 * Matches a player whose life total is exactly {@code life} ("each player with exactly 13 life", Triskaidekaphobia;
 * "an opponent with the most life among your opponents" is {@code HighestOpponentLifeTotal} over
 * {@code Subjects.eachOpponent()}, The Master, Gallifrey's End). Used with {@code Subjects.matching(...)}; the life
 * totals and {@code life}, evaluated for the effect's controller, are read as the effect applies (CR 608.2h).
 */
public record PlayerLifeExactlyPredicate(DynamicAmount life) implements PlayerPredicate {

    public PlayerLifeExactlyPredicate {
        Objects.requireNonNull(life, "life");
    }

    public PlayerLifeExactlyPredicate(int life) {
        this(new Fixed(life));
    }
}
