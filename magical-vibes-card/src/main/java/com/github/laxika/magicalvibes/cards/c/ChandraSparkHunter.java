package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnControllerArtifactEntersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "116")
public class ChandraSparkHunter extends Card {

    public ChandraSparkHunter() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                "Target must be a Vehicle you control"), 0, 1)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new AnimatePermanentsEffect(
                        null, null, List.of(), Set.of(Keyword.HASTE), null, Set.of(CardType.CREATURE),
                        GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN, null));

        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new MayEffect(
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Sacrifice an artifact. If you do, draw a card",
                                        new SacrificePermanentThenEffect(
                                                new PermanentIsArtifactPredicate(), new DrawCardEffect(1), "an artifact")),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Discard a card. If you do, draw a card",
                                        new DiscardCardThenEffect(
                                                null, new DrawCardEffect(1), "a card"))
                        )),
                        "You may sacrifice an artifact or discard a card.")),
                "+2: You may sacrifice an artifact or discard a card. If you do, draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(new CreateTokenEffect(
                        CardType.ARTIFACT,
                        1,
                        "Vehicle",
                        3,
                        2,
                        null,
                        null,
                        List.of(CardSubtype.VEHICLE),
                        Set.of(),
                        Set.of(),
                        false,
                        false,
                        Map.of(),
                        List.of(new ActivatedAbility(
                                false,
                                null,
                                List.of(new CrewCost(1), AnimatePermanentsEffect.crew()),
                                "Crew 1")),
                        false,
                        false,
                        false,
                        0,
                        Set.of()
                )),
                "0: Create a 3/2 colorless Vehicle artifact token with crew 1."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(new DealDamageToAnyTargetOnControllerArtifactEntersEffect(3)),
                        "Whenever an artifact you control enters, this emblem deals 3 damage to any target.")),
                "−7: You get an emblem with \"Whenever an artifact you control enters, this emblem deals "
                        + "3 damage to any target.\""
        ));
    }
}
