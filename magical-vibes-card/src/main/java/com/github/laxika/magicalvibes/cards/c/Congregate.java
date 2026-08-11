package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "M14", collectorNumber = "14")
@CardRegistration(set = "M15", collectorNumber = "6")
@CardRegistration(set = "USG", collectorNumber = "8")
public class Congregate extends Card {

    public Congregate() {
        // Target player gains 2 life for each creature on the battlefield.
        addEffect(EffectSlot.SPELL, new TargetPlayerGainsLifeEffect(
                new Scaled(new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER), 2)));
    }
}
