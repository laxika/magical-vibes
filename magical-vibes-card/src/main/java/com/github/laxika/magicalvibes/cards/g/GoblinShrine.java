package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "50")
public class GoblinShrine extends Card {

    public GoblinShrine() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.STATIC, new ConditionalEffect(
                        new EnchantedPermanentMatches(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentHasSupertypePredicate(CardSupertype.BASIC),
                                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)
                                )),
                                "enchanted land is a basic Mountain"),
                        new StaticBoostEffect(1, 0, GrantScope.ALL_CREATURES,
                                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN))
                ))
                .addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new MassDamageEffect(
                        1,
                        false,
                        false,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)
                        ))
                ));
    }
}
