package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSubtypeEffect;

import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "18")
public class KinjallisCaller extends Card {

    public KinjallisCaller() {
        // Dinosaur spells you cast cost {1} less to cast
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostForSubtypeEffect(
                Set.of(CardSubtype.DINOSAUR), 1));
    }
}
