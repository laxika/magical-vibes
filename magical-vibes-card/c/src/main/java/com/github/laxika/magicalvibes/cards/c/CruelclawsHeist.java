package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNonlandCardFromTargetHandOrGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "BLB", collectorNumber = "88")
public class CruelclawsHeist extends Card {

    public CruelclawsHeist() {
        addStaticEffect(new GiftEffect());
        // The gift goes to the chosen opponent, not to the target (CR 702.174a): with two players, the opponent.
        addSpellEffect(new ConditionalEffect(new GiftPromised(), new EachOtherPlayerDrawsCardEffect(1)));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(new ConditionalEffect(new GiftPromised(),
                        new ExileNonlandCardFromTargetHandOrGraveyardEffect(true, true)))
                .addSpellEffect(new ConditionalEffect(new NotCondition(new GiftPromised()),
                                new ExileNonlandCardFromTargetHandOrGraveyardEffect(false, true)));
    }
}
