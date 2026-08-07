package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreaturesForCostReductionEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "DOM", collectorNumber = "108")
public class TorgaarFamineIncarnate extends Card {

    public TorgaarFamineIncarnate() {
        addEffect(EffectSlot.STATIC, new SacrificeCreaturesForCostReductionEffect(2));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ), 0, 1).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                // "half their starting life total, rounded down"
                new SetLifeTotalEffect(GameData.STARTING_LIFE_TOTAL / 2, SetLifeTotalRecipient.TARGET_PLAYER));
    }
}
