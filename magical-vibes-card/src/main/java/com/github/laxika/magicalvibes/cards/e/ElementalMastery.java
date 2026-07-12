package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "90")
public class ElementalMastery extends Card {

    public ElementalMastery() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new CreateTokenEffect(
                                CardType.CREATURE,
                                new SourcePower(),
                                "Elemental", 1, 1,
                                CardColor.RED, null,
                                List.of(CardSubtype.ELEMENTAL),
                                Set.of(Keyword.HASTE),
                                Set.of(),
                                false, false,
                                Map.of(), List.of(),
                                false, true, false, 0, Set.of())),
                        "{T}: Create X 1/1 red Elemental creature tokens with haste, where X is this creature's power. Exile them at the beginning of the next end step."
                ),
                GrantScope.ENCHANTED_CREATURE
        ));
    }
}
