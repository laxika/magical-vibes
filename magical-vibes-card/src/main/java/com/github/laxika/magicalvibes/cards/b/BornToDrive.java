package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndSaddleEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "6")
public class BornToDrive extends Card {

    public BornToDrive() {
        PermanentPredicate artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        PermanentPredicate creatureOrVehicle = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));
        PermanentCount creaturesAndVehicles = new PermanentCount(creatureOrVehicle, CountScope.CONTROLLER);

        target(new PermanentPredicateTargetFilter(
                artifactOrCreature,
                "Target must be an artifact or creature"))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(),
                        new DynamicStaticBoostEffect(
                                creaturesAndVehicles, creaturesAndVehicles, GrantScope.ENCHANTED_PERMANENT),
                        null));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new CreateTokenEffect(
                        2,
                        "Pilot",
                        1,
                        1,
                        null,
                        List.of(CardSubtype.PILOT),
                        Set.of(),
                        Set.of(),
                        Map.of(EffectSlot.STATIC, new PowerBoostForCrewAndSaddleEffect(2)))),
                "Channel — {2}{W}, Discard this card: Create two 1/1 colorless Pilot creature tokens with \"This token crews Vehicles as though its power were 2 greater.\""
        ));
    }
}
