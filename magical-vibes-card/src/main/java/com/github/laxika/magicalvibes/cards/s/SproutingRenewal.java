package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "145")
public class SproutingRenewal extends Card {

    public SproutingRenewal() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 2/2 green and white Elf Knight creature token with vigilance",
                        new CreateTokenEffect(
                                1, "Elf Knight", 2, 2, CardColor.GREEN,
                                Set.of(CardColor.GREEN, CardColor.WHITE),
                                List.of(CardSubtype.ELF, CardSubtype.KNIGHT),
                                Set.of(Keyword.VIGILANCE), Set.of()
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target artifact or enchantment",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsEnchantmentPredicate()
                                )),
                                "Target must be an artifact or enchantment."
                        )
                )
        )));
    }
}
