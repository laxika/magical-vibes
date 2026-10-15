package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOpponentOfSourceControllerPredicate;

@CardRegistration(set = "GN2", collectorNumber = "3")
public class CalculatingLich extends Card {

    public CalculatingLich() {
        // Whenever a creature attacks one of your opponents, that player loses 1 life.
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, new TriggeringPermanentConditionalEffect(
                new PermanentIsAttackingOpponentOfSourceControllerPredicate(),
                new LoseLifeEffect(1, LoseLifeRecipient.DEFENDING_PLAYER)));
    }
}
