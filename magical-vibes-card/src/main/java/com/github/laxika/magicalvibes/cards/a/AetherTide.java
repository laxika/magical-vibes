package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "EXO", collectorNumber = "27")
public class AetherTide extends Card {

    public AetherTide() {
        addEffect(EffectSlot.SPELL, new DiscardXCardsCost(
                new CardTypePredicate(CardType.CREATURE), "creature cards"));
        targetX(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Targets must be creatures"
        ), 100).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
