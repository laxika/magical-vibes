package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandElseGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MRD", collectorNumber = "43")
public class NeurokFamiliar extends Card {

    public NeurokFamiliar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RevealTopCardMatchingToHandElseGraveyardEffect(new CardTypePredicate(CardType.ARTIFACT)));
    }
}
