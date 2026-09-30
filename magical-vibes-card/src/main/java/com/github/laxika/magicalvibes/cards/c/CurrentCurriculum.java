package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YECL", collectorNumber = "2")
public class CurrentCurriculum extends Card {

    public CurrentCurriculum() {
        // The first Merfolk spell you cast each turn has convoke.
        addEffect(EffectSlot.STATIC, new GrantSpellCastingAbilityToFirstMatchingSpellEachTurnEffect(
                Keyword.CONVOKE, new CardSubtypePredicate(CardSubtype.MERFOLK)));

        // At the beginning of your end step, if you control two or more tapped creatures,
        // conjure a tapped Stonybrook Schoolmaster onto the battlefield.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(2, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate()))),
                new ConjureCardNamedOntoBattlefieldEffect(
                        "MOR", "25", Set.of(CardType.CREATURE))));
    }
}
