package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardGreatestManaValueNonlandCardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMID", collectorNumber = "27")
public class CitystalkerConnoisseur extends Card {

    public CitystalkerConnoisseur() {
        var discardGreatestManaValueNonlandCard = new DiscardGreatestManaValueNonlandCardEffect();
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).bindEffect(discardGreatestManaValueNonlandCard);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                discardGreatestManaValueNonlandCard,
                CreateTokenEffect.ofBloodToken(1));
    }
}
