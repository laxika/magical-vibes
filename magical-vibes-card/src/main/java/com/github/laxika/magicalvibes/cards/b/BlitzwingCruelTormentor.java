package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "BOT", collectorNumber = "4")
@CardRegistration(set = "BOT", collectorNumber = "19")
public class BlitzwingCruelTormentor extends Card {

    public BlitzwingCruelTormentor() {
        setBackFaceCard(new BlitzwingAdaptiveAssailant());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{3}{B}"));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                ConditionalEffect.unless(
                        new TargetPlayerLostLifeThisTurn(),
                        new LoseLifeEffect(new LifeLostThisTurn(CountScope.TARGET_PLAYER),
                                LoseLifeRecipient.TARGET_PLAYER)),
                ConditionalEffect.unless(
                        new NotCondition(new TargetPlayerLostLifeThisTurn()),
                        new TransformSelfEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "BlitzwingAdaptiveAssailant";
    }
}
