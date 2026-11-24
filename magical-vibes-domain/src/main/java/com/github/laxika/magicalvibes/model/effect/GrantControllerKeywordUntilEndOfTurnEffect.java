package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Objects;
import java.util.Set;

/**
 * One-shot effect: each player {@code players} names has the given player keyword until end of turn, or for the
 * stated {@code duration} ("you gain hexproof until your next turn", Blossoming Calm; "players gain hexproof until
 * end of turn", Everybody Lives!). Floated on each player as a player-scoped floating effect, read by
 * {@code GameQueryService.playerHasShroud} / {@code playerHasHexproof} / {@code playerHasFlying}. Flying is the
 * engine-built float of Sarah's Wings' player branch.
 *
 * @param keyword  shroud, hexproof or flying
 * @param players  who gains it; the controller by default; never a target
 * @param duration how long it lasts
 */
public record GrantControllerKeywordUntilEndOfTurnEffect(Keyword keyword, PlayerSubject players, Duration duration)
        implements CardEffect {

    private static final Set<Keyword> PLAYER_KEYWORDS = Set.of(Keyword.SHROUD, Keyword.HEXPROOF, Keyword.FLYING);

    public GrantControllerKeywordUntilEndOfTurnEffect(Keyword keyword) {
        this(keyword, Subjects.you(), Duration.UNTIL_END_OF_TURN);
    }

    public GrantControllerKeywordUntilEndOfTurnEffect(Keyword keyword, Duration duration) {
        this(keyword, Subjects.you(), duration);
    }

    public GrantControllerKeywordUntilEndOfTurnEffect {
        if (!PLAYER_KEYWORDS.contains(keyword)) {
            throw new IllegalArgumentException(
                    "GrantControllerKeywordUntilEndOfTurnEffect supports only SHROUD, HEXPROOF and FLYING, got " + keyword);
        }
        Objects.requireNonNull(players, "players");
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("GrantControllerKeywordUntilEndOfTurnEffect takes no target player");
        }
        Objects.requireNonNull(duration, "duration");
    }

    /** "Players gain [keyword] until end of turn." */
    public static GrantControllerKeywordUntilEndOfTurnEffect eachPlayer(Keyword keyword) {
        return new GrantControllerKeywordUntilEndOfTurnEffect(keyword, Subjects.eachPlayer(), Duration.UNTIL_END_OF_TURN);
    }
}
