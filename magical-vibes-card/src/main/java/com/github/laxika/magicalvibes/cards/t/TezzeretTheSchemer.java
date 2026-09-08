package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemTriggerStep;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AER", collectorNumber = "137")
public class TezzeretTheSchemer extends Card {

    private static final String EMBLEM_TEXT =
            "At the beginning of combat on your turn, target artifact you control becomes an artifact creature with base power and toughness 5/5.";

    public TezzeretTheSchemer() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(etheriumCellToken()),
                "+1: Create a colorless artifact token named Etherium Cell with \"{T}, Sacrifice this token: Add one mana of any color.\""
        ));

        PermanentCount artifacts = new PermanentCount(
                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new BoostTargetCreatureEffect(artifacts, new Scaled(artifacts, -1))),
                "−2: Target creature gets +X/−X until end of turn, where X is the number of artifacts you control.",
                TargetFilters.creature()
        ));

        AnimatePermanentsEffect animateArtifact = new AnimatePermanentsEffect(
                new Fixed(5), new Fixed(5), List.of(), Set.of(), null, Set.of(CardType.CREATURE),
                GrantScope.TARGET, EffectDuration.PERMANENT,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentControlledBySourceControllerPredicate())));
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new CreateEmblemEffect(
                        List.of(new EmblemStepTriggerEffect(
                                EmblemTriggerStep.BEGINNING_OF_COMBAT,
                                List.of(animateArtifact),
                                EMBLEM_TEXT)),
                        EMBLEM_TEXT)),
                "−7: You get an emblem with \"" + EMBLEM_TEXT + "\""
        ));
    }

    private static CreateTokenEffect etheriumCellToken() {
        return CreateTokenEffect.ofArtifactToken(
                1,
                "Etherium Cell",
                List.of(),
                List.of(new ActivatedAbility(
                        true,
                        null,
                        List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                        "{T}, Sacrifice this token: Add one mana of any color."
                )));
    }
}
