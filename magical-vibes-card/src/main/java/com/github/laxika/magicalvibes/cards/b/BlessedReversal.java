package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;

@CardRegistration(set = "POR", collectorNumber = "7")
@CardRegistration(set = "8ED", collectorNumber = "7")
public class BlessedReversal extends Card {

    public BlessedReversal() {
        // You gain 3 life for each creature attacking you.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Scaled(
                new PermanentCount(new PermanentIsAttackingSourceControllerPredicate(), CountScope.ANY_PLAYER), 3)));
    }
}
