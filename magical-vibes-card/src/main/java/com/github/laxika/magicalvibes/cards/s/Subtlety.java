package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfIfEvokedEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPG", collectorNumber = "45")
@CardRegistration(set = "SPG", collectorNumber = "50")
public class Subtlety extends Card {

    public Subtlety() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.CREATURE_SPELL,
                        StackEntryType.PLANESWALKER_SPELL)),
                "Target must be a creature or planeswalker spell."), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect());

        addCastingOption(new AlternateHandCast(List.of(
                new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.BLUE), "blue"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeSelfIfEvokedEffect());
    }
}
