package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "ISD", collectorNumber = "49")
public class Curiosity extends Card {

    public Curiosity() {
        setTargetFilter(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));

        // Whenever enchanted creature deals damage to an opponent, you may draw a card.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
