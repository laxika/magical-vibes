package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "136")
public class LeylineInvocation extends Card {

    public LeylineInvocation() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Fractal", 0, 0, CardColor.GREEN,
                Set.of(CardColor.GREEN, CardColor.BLUE), List.of(CardSubtype.FRACTAL)));
        addEffect(EffectSlot.SPELL, new PutCountersOnCreatedPermanentsEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER)));
    }
}
