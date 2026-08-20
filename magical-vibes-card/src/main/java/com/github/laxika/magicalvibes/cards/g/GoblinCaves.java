package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "64")
public class GoblinCaves extends Card {

    public GoblinCaves() {
        target(TargetFilters.land());

        addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.BASIC),
                        new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN))),
                new StaticBoostEffect(0, 2, GrantScope.ALL_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)),
                null));
    }
}
