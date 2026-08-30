package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "CSP", collectorNumber = "96")
public class RiteOfFlame extends Card {

    public RiteOfFlame() {
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.RED, new Sum(
                new Fixed(2),
                new CardsInGraveyard(new CardNamedPredicate("Rite of Flame"), CountScope.ANY_PLAYER))));
    }
}
