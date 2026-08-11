package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsYouPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "53")
public class CeruleanDrake extends Card {

    public CeruleanDrake() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.RED)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new CounterSpellEffect()),
                "Sacrifice this creature: Counter target spell that targets you.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.CREATURE_SPELL,
                                        StackEntryType.ENCHANTMENT_SPELL,
                                        StackEntryType.SORCERY_SPELL,
                                        StackEntryType.INSTANT_SPELL,
                                        StackEntryType.ARTIFACT_SPELL,
                                        StackEntryType.PLANESWALKER_SPELL,
                                        StackEntryType.BATTLE_SPELL)),
                                new StackEntryTargetsYouPredicate()
                        )),
                        "Target must be a spell that targets you."
                )
        ));
    }
}
