package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect that sets up a turn-long replacement effect (MTG Rule 614.1c): for the rest of
 * this turn, each creature the effect's controller controls enters the battlefield with
 * {@code count} additional +1/+1 counters on it. {@link Duration#UNTIL_YOUR_NEXT_TURN}
 * extends the replacement effect through the controller's next turn.
 * <p>
 * Unlike {@link ControlledCreaturesEnterWithAdditionalCountersEffect} this is not tied to a static
 * ability of a permanent on the battlefield — it is a floating effect on the controller made as the ability
 * resolves, so it keeps working even if the source leaves.
 * <p>
 * Used by Zameck Guildmage ("This turn, each creature you control enters with an additional
 * +1/+1 counter on it.").
 */
public record ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect(int count,
                                                                             Duration duration)
        implements CardEffect {

    public ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect(int count) {
        this(count, Duration.UNTIL_END_OF_TURN);
    }
}
