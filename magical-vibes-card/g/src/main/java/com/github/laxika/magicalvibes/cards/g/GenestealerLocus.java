package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOpponentOfSourceControllerPredicate;

@CardRegistration(set = "40K", collectorNumber = "21")
public class GenestealerLocus extends Card {

    public GenestealerLocus() {
        // Whenever a creature attacks you, it gets -1/-0 until end of turn.
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU_DIRECTLY, new BoostTargetCreatureEffect(-1, 0));

        // Whenever a creature attacks one of your opponents, it gets +0/+1 until end of turn.
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, new TriggeringPermanentConditionalEffect(
                new PermanentIsAttackingOpponentOfSourceControllerPredicate(),
                new BoostTargetCreatureEffect(0, 1)));
    }
}
