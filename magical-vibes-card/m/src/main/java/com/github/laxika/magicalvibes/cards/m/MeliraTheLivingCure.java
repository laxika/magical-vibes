package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.MeliraPoisonReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardOnDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "209")
public class MeliraTheLivingCure extends Card {

    public MeliraTheLivingCure() {
        addEffect(EffectSlot.STATIC, new MeliraPoisonReplacementEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ExileSelfCost(), new ReturnTargetCardOnDeathThisTurnEffect()),
                "Exile Melira, the Living Cure: Choose another target creature or artifact. When it's put into a graveyard this turn, return that card to the battlefield under its owner's control.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsArtifactPredicate())))),
                        "Target must be another creature or artifact"
                )
        ));
    }
}
