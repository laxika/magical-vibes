package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringArtifactControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "118")
public class SusurianVoidborn extends Card {

    private static final SequenceEffect DRAIN = SequenceEffect.of(
            new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
            new GainLifeEffect(1));

    public SusurianVoidborn() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{B}"))));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ));

        addEffect(EffectSlot.ON_DEATH, DRAIN);
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate()), DRAIN));
        addEffect(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringArtifactControllerConditionalEffect(DRAIN));
    }
}
