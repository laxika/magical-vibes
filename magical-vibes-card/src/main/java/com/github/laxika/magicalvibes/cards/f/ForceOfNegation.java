package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "52")
public class ForceOfNegation extends Card {

    public ForceOfNegation() {
        // If it's not your turn, you may exile a blue card from your hand rather than pay this
        // spell's mana cost.
        addCastingOption(new AlternateHandCast(
                List.of(new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.BLUE), "blue")),
                new NotControllerTurn(),
                false));

        // Counter target noncreature spell. If that spell is countered this way, exile it instead
        // of putting it into its owner's graveyard.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))),
                "Target must be a noncreature spell."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect(CounteredSpellDestination.EXILE));
    }
}
