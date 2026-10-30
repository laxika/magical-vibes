package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "23")
public class BrazenBoarding extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "236"),
            new DraftFromSpellbookEffect.SpellbookCard("ATQ", "56"),
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "239"),
            new DraftFromSpellbookEffect.SpellbookCard("MMQ", "304"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "244"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "242"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "236"),
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "247"),
            new DraftFromSpellbookEffect.SpellbookCard("RIX", "181"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "244"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "241"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "252"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "248"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "251"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "219"));

    public BrazenBoarding() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                "Target must be a creature or planeswalker"))
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureOrPlaneswalkerEffect(4))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new EventValueAtLeast(1), SequenceEffect.of(
                                new ConditionalEffect(new EventValueAtLeast(4),
                                        new ConjureCardToBattlefieldEffect("XLN", "217")),
                                new ConditionalEffect(new NotCondition(new EventValueAtLeast(4)),
                                        new ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect(
                                                SPELLBOOK, new EventValue())))));
    }
}
