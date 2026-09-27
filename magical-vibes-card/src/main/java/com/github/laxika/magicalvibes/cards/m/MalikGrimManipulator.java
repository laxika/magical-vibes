package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerAndTargetPlayerChooseCreaturesThenSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "SLD", collectorNumber = "147")
@CardRegistration(set = "SLX", collectorNumber = "23")
public class MalikGrimManipulator extends Card {

    public MalikGrimManipulator() {
        target(opponentTarget())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ControllerAndTargetPlayerChooseCreaturesThenSacrificeEffect());

        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsCreaturePredicate(),
                        CreateTokenEffect.ofTreasureToken(1)));
    }

    private static PlayerPredicateTargetFilter opponentTarget() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
    }
}
