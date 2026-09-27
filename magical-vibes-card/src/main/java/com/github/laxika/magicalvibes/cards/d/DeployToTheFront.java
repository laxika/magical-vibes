package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "C14", collectorNumber = "6")
public class DeployToTheFront extends Card {

    public DeployToTheFront() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.whiteSoldier(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER)));
    }
}
