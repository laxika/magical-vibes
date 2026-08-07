package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileTopCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WTH", collectorNumber = "81")
public class SpinningDarkness extends Card {

    public SpinningDarkness() {
        // You may exile the top three black cards of your graveyard rather than pay this spell's mana cost.
        addCastingOption(new AlternateHandCast(List.of(
                new ExileTopCardsFromGraveyardCastingCost(new CardColorPredicate(CardColor.BLACK), "black", 3))));

        // Spinning Darkness deals 3 damage to target nonblack creature.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK))))),
                "Target must be a nonblack creature"));
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));

        // You gain 3 life.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
    }
}
