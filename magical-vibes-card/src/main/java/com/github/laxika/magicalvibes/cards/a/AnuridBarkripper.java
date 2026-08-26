package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "JUD", collectorNumber = "104")
public class AnuridBarkripper extends Card {

    public AnuridBarkripper() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GraveyardCardThreshold(7, new CardTruePredicate()),
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}
