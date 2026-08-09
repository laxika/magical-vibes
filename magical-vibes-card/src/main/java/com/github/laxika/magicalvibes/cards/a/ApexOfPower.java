package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayCastMatchingThisTurnEffect;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M19", collectorNumber = "129")
public class ApexOfPower extends Card {

    public ApexOfPower() {
        addEffect(EffectSlot.SPELL,
                new ExileTopCardsMayCastMatchingThisTurnEffect(
                        7, new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        addEffect(EffectSlot.SPELL,
                new ConditionalEffect(new CastFromZone(Zone.HAND), new AwardAnyColorManaEffect(10)));
    }
}
