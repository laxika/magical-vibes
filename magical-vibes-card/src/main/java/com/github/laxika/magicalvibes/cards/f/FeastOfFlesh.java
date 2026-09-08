package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "CSP", collectorNumber = "56")
public class FeastOfFlesh extends Card {

    public FeastOfFlesh() {
        var amount = new Sum(
                new Fixed(1),
                new CardsInGraveyard(new CardNamedPredicate("Feast of Flesh"), CountScope.ANY_PLAYER));
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(amount));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(amount));
    }
}
