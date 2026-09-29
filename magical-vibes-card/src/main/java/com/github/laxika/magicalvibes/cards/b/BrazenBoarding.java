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
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "23")
public class BrazenBoarding extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Colossal Plow",
            "Millstone",
            "Whirlermaker",
            "Magistrate's Scepter",
            "Replicating Ring",
            "Raiders' Karve",
            "Weapon Rack",
            "Relic Amulet",
            "Orazca Relic",
            "Fifty Feet of Rope",
            "Pyre of Heroes",
            "Treasure Chest",
            "Leather Armor",
            "Spiked Pit Trap",
            "Gingerbrute");

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
                                        new ConjureCardToBattlefieldEffect("Admiral Beckett Brass")),
                                new ConditionalEffect(new NotCondition(new EventValueAtLeast(4)),
                                        new ConjureRandomCardFromSpellbookWithManaValueToBattlefieldEffect(
                                                SPELLBOOK, new EventValue())))));
    }
}
