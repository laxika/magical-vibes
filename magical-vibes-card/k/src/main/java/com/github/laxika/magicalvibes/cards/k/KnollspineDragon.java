package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "98")
@CardRegistration(set = "GN3", collectorNumber = "82")
@CardRegistration(set = "LTC", collectorNumber = "224")
public class KnollspineDragon extends Card {

    public KnollspineDragon() {
        // Flying — auto-loaded from Scryfall.
        //
        // When this creature enters, you may discard your hand and draw cards equal to the
        // damage dealt to target opponent this turn.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new DiscardOwnHandThenDrawEffect(new DamageDealtToTargetPlayerThisTurn()),
                "discard your hand and draw cards equal to the damage dealt to target opponent this turn"));
    }
}
