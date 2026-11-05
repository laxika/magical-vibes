package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.PermanentChangeTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads the zone-change and permanent-change abilities (every {@link ZoneChangeTrigger} but "when this enters",
 * {@link ZoneChangeTrigger#ownEntry()}, and every {@link PermanentChangeTrigger}) for the readers outside the trigger
 * pipeline; none of them has a legacy view. The pipeline ({@code TriggeredAbilityStackService}) judges their filters
 * and options as it matches the event. The readers here put an ability on the stack from what is not an object the
 * pipeline lists: the face-up planes (CR 901.7), the face-up schemes, and the abilities a resolved spell or ability
 * registered (CR 603.7). They judge the trigger and the event themselves and read no option, so they take the effects
 * of an ability without options and throw on any other, where they dropped the options before.
 */
public final class ZoneChangeAbilityReader {

    private ZoneChangeAbilityReader() {
    }

    /**
     * The abilities of {@code card} with {@code trigger} as a trigger condition, in whatever zone it functions, in
     * the order they were added: a face-up plane's, which are read wherever the plane is.
     */
    public static List<TriggeredAbility> printed(Card card, Trigger trigger) {
        return card.getTriggeredAbilities(trigger);
    }

    /**
     * The effects of {@code abilities}, in order, for a reader that judges none of their options: throws when one has
     * an option.
     */
    public static List<CardEffect> strictEffects(List<TriggeredAbility> abilities) {
        List<CardEffect> effects = new ArrayList<>();
        for (TriggeredAbility ability : abilities) {
            requireNothingToJudge(ability);
            ability.effects().forEach(registration -> effects.add(registration.effect()));
        }
        return effects;
    }

    /**
     * The effect of {@code ability}, a registered ability that its reader puts on the stack as one effect: throws when
     * it has an option or more than one effect, which the reader would drop.
     */
    public static CardEffect strictEffect(TriggeredAbility ability) {
        requireNothingToJudge(ability);
        if (ability.effects().size() != 1) {
            throw new IllegalStateException("The reader of a registered zone-change ability puts one effect on the "
                    + "stack: " + ability);
        }
        return ability.effects().getFirst().effect();
    }

    /** Throws when {@code ability} has an option, which the readers here don't judge. */
    public static void requireNothingToJudge(TriggeredAbility ability) {
        if (!ability.options().equals(TriggerOptions.DEFAULT)) {
            throw new IllegalStateException("No zone-change reader outside the trigger pipeline reads "
                    + ability.options() + ": " + ability);
        }
    }
}
