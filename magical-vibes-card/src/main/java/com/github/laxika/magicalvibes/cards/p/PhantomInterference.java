package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "61")
public class PhantomInterference extends Card {

    public PhantomInterference() {
        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{3}", "{1}")));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 2/2 white Spirit creature token with flying",
                        new CreateTokenEffect(1, "Spirit", 2, 2, CardColor.WHITE,
                                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of())),
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell unless its controller pays {2}",
                        new CounterUnlessPaysEffect(2),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.CREATURE_SPELL,
                                        StackEntryType.ENCHANTMENT_SPELL,
                                        StackEntryType.SORCERY_SPELL,
                                        StackEntryType.INSTANT_SPELL,
                                        StackEntryType.ARTIFACT_SPELL,
                                        StackEntryType.PLANESWALKER_SPELL,
                                        StackEntryType.BATTLE_SPELL)),
                                "Target must be a spell."))
        )));
    }
}
