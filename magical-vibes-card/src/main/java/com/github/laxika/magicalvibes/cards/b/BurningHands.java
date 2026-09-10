package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfTargetMatches;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "135")
public class BurningHands extends Card {

    public BurningHands() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                new FixedIfTargetMatches(
                        new PermanentColorInPredicate(Set.of(CardColor.GREEN)),
                        6,
                        2
                )));
    }
}
