package com.github.laxika.magicalvibes.cards.r;

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
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "355")
public class RotLikeTheScumYouAre extends Card {

    public RotLikeTheScumYouAre() {
        addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new CreateTokenEffect("Ooze", 2, 2, CardColor.GREEN, List.of(CardSubtype.OOZE),
                        Set.of(), Set.of()),
                new PutCountersOnCreatedPermanentsEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new PermanentCount(new PermanentIsLandPredicate(), CountScope.OPPONENTS))));
    }
}
