package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfReturnAtNextUpkeepWithHasteEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "GTC", collectorNumber = "182")
public class ObzedatGhostCouncil extends Card {

    public ObzedatGhostCouncil() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new MayEffect(new ExileSelfReturnAtNextUpkeepWithHasteEffect(),
                        "Exile Obzedat, Ghost Council? It returns at the beginning of your next upkeep with haste."));
    }
}
