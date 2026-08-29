package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DiscardCardCastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "90")
public class Flameshot extends Card {

    public Flameshot() {
        addCastingOption(new AlternateHandCast(List.of(
                new DiscardCardCastingCost(new CardSubtypePredicate(CardSubtype.MOUNTAIN), "a Mountain card"))));
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature."
        ), 1, 3).addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreatures(3));
    }
}
