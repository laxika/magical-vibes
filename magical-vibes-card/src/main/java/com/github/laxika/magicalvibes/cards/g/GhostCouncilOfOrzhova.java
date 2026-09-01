package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "114")
public class GhostCouncilOfOrzhova extends Card {

    public GhostCouncilOfOrzhova() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(),
                        FlickerEffect.exileSelfReturnAtEndStepUnderOwnerControl(false)
                ),
                "{1}, Sacrifice a creature: Exile Ghost Council of Orzhova. Return it to the battlefield under its owner's control at the beginning of the next end step."
        ));
    }
}
