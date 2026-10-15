package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "TMP", collectorNumber = "192")
public class MoggSquad extends Card {

    public MoggSquad() {
        // Mogg Squad gets -1/-1 for each other creature on the battlefield.
        final Scaled penalty = new Scaled(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER, true), -1);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(penalty, penalty));
    }
}
