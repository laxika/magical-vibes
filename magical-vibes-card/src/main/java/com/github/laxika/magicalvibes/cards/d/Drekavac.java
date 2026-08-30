package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DIS", collectorNumber = "43")
public class Drekavac extends Card {

    public Drekavac() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                SacrificeUnlessDiscardCardTypeEffect.forPredicate(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        "noncreature card"));
    }
}
