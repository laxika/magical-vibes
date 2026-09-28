package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "208")
public class DruidicRitual extends Card {

    public DruidicRitual() {
        addEffect(EffectSlot.SPELL, new MayEffect(
                new MillEffect(3, MillRecipient.CONTROLLER), "Mill three cards?"));
        addEffect(EffectSlot.SPELL, new ReturnUpToOneOfEachFilterFromGraveyardToHandEffect(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.LAND))));
    }
}
