package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateSetSelfBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "RNA", collectorNumber = "92")
public class Amplifire extends Card {

    public Amplifire() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RevealUntilCardPredicateSetSelfBasePowerToughnessEffect(
                        new CardTypePredicate(CardType.CREATURE), 2));
    }
}
