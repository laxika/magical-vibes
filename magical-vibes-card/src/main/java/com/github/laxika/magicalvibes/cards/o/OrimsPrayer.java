package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;

@CardRegistration(set = "TMP", collectorNumber = "32")
public class OrimsPrayer extends Card {

    public OrimsPrayer() {
        // Whenever one or more creatures attack you, you gain 1 life for each attacking creature.
        addEffect(EffectSlot.ON_CREATURES_ATTACK_YOU, new GainLifeEffect(
                new PermanentCount(new PermanentIsAttackingSourceControllerPredicate(), CountScope.ANY_PLAYER)));
    }
}
