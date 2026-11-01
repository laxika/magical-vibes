package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.i.InfectiousCurse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReduceOpponentCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardAttachedToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "97")
@CardRegistration(set = "SIR", collectorNumber = "97")
public class AccursedWitch extends Card {

    public AccursedWitch() {
        setBackFaceCard(new InfectiousCurse());

        addStaticEffect(new ReduceOpponentCostForTargetingControlledPermanentEffect(
                new PermanentIsSourcePermanentPredicate(), 1));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.dies(ObjectRelation.THIS),
                new ReturnSourceTransformedFromGraveyardAttachedToTargetPlayerEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "InfectiousCurse";
    }
}
