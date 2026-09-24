package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySacrificeMatchingPermanentOrLoseLifeAndControllerDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "84")
@CardRegistration(set = "DMU", collectorNumber = "288")
public class BraidsArisenNightmare extends Card {

    private static final PermanentAnyOfPredicate SACRIFICEABLE_PERMANENT = new PermanentAnyOfPredicate(
            List.of(
                    new PermanentIsArtifactPredicate(),
                    new PermanentIsCreaturePredicate(),
                    new PermanentIsEnchantmentPredicate(),
                    new PermanentIsLandPredicate(),
                    new PermanentIsPlaneswalkerPredicate()));

    public BraidsArisenNightmare() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new MayEffect(
                        new SacrificePermanentThenEffect(
                                SACRIFICEABLE_PERMANENT,
                                new EachOpponentMaySacrificeMatchingPermanentOrLoseLifeAndControllerDrawsEffect(),
                                "an artifact, creature, enchantment, land, or planeswalker",
                                false,
                                false),
                        "Sacrifice an artifact, creature, enchantment, land, or planeswalker?"));
    }
}
