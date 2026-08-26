package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnCreatedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

public class LastingFayth extends Card {

    public LastingFayth() {
        addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new CreateTokenEffect("Hero", 1, 1, null, List.of(CardSubtype.HERO), Set.of(), Set.of()),
                new PutCountersOnCreatedPermanentsEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER))));
    }
}
