package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFromExileConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentMayPlayWithOpponentTaxEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WOE", collectorNumber = "48")
@CardRegistration(set = "WOE", collectorNumber = "331")
public class ExtraordinaryJourney extends Card {

    public ExtraordinaryJourney() {
        targetUpTo(new XValue(), TargetFilters.creature(), 100)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentMayPlayWithOpponentTaxEffect(0));
        // The draw trigger also sees Extraordinary Journey's own entry when it is a creature (Opalescence).
        // The any-other slot skips the source, so the self-or-ally slot covers that entry; the once-each-turn
        // limit keeps the two from both drawing.
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, drawWhenCreatureEntersFromExile());
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD, drawWhenCreatureEntersFromExile());
    }

    private static EnteringCreatureFromExileConditionalEffect drawWhenCreatureEntersFromExile() {
        return new EnteringCreatureFromExileConditionalEffect(
                new OncePerTurnTriggerEffect(new DrawCardEffect(1)));
    }
}
