package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/**
 * Gives the resolving spell or ability's controller a boon: a delayed triggered ability (CR 603.7a) with no stated
 * duration that triggers {@code uses} times ("you get a two-time boon with 'whenever you sacrifice one or more
 * permanents, seek a nonland card'", Tasteful Offering), its source the maker's (CR 603.7d, 603.7e).
 *
 * @param uses             how many times it triggers before it ends
 * @param ability          the boon's ability
 * @param targetFilter     what its targets, chosen as it goes on the stack (CR 603.3d), must match; {@code null} = the
 *                         effects' own
 * @param usedAsItResolves whether its own ability uses it up as it resolves ({@link ConsumeBoonEffect}) instead of
 *                         as it triggers: "you get a one-time boon with 'at the beginning of your end step, if ...,
 *                         ...'" (Underbridge Warlock) triggers at each such step whose "if" holds until it has resolved
 */
public record CreateBoonEffect(int uses, TriggeredAbility ability, TargetFilter targetFilter,
                               boolean usedAsItResolves) implements CardEffect {

    public CreateBoonEffect {
        if (uses <= 0) {
            throw new IllegalArgumentException("A boon must have at least one use");
        }
        if (ability == null) {
            throw new IllegalArgumentException("A boon has an ability");
        }
    }

    /** A {@code uses}-time boon with {@code ability}. */
    public CreateBoonEffect(int uses, TriggeredAbility ability) {
        this(uses, ability, null, false);
    }

    /** A {@code uses}-time boon with {@code ability}, its targets restricted to {@code targetFilter}. */
    public CreateBoonEffect(int uses, TriggeredAbility ability, TargetFilter targetFilter) {
        this(uses, ability, targetFilter, false);
    }

    /** A one-time boon with {@code ability}, which its ability uses up as it resolves ({@link ConsumeBoonEffect}). */
    public static CreateBoonEffect usedAsItResolves(TriggeredAbility ability) {
        return new CreateBoonEffect(1, ability, null, true);
    }
}
