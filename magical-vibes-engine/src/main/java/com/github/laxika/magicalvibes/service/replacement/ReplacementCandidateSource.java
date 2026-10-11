package com.github.laxika.magicalvibes.service.replacement;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.replacement.ReplacementEvent;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;

import java.util.List;

/**
 * One family of replacement and/or prevention effects that can modify a kind of
 * {@link ReplacementEvent}. Every {@code @Component} implementing this is consulted by
 * {@link ReplacementOrderService}, so a new effect joins the CR 616.1 ordering by existing as a
 * bean — no event site has to learn about it.
 *
 * @param <E> the event kind this family modifies
 */
public interface ReplacementCandidateSource<E extends ReplacementEvent> {

    Class<E> eventType();

    /**
     * The effects of this family that would modify {@code event} as it stands now, one option per
     * effect instance. Must not change the game. Options whose key is already in
     * {@code event.appliedKeys()} are filtered out by the caller.
     */
    List<ReplacementOption> applicable(GameData gameData, E event);

    /**
     * {@code event} as modified by {@code option}. Pure: no game change and no log, so it is safe
     * for previews; only called for options flagged {@link ReplacementOption#pure()}.
     */
    E transform(GameData gameData, E event, ReplacementOption option);

    /** Applies {@code option} to the real game. */
    default ApplyResult<E> apply(GameData gameData, E event, ReplacementOption option) {
        return new ApplyResult.Applied<>(transform(gameData, event, option));
    }

    /** What applying one effect did to the event. */
    sealed interface ApplyResult<E extends ReplacementEvent> {

        /** The effect has been applied; {@code event} is the modified event. */
        record Applied<E extends ReplacementEvent>(E event) implements ApplyResult<E> {}

        /**
         * The effect asked a player for a decision of its own (a "you may"); {@code event} must be
         * resumed through {@link ReplacementOrderService#run} once that decision is made.
         */
        record Suspended<E extends ReplacementEvent>(E event) implements ApplyResult<E> {}
    }
}
