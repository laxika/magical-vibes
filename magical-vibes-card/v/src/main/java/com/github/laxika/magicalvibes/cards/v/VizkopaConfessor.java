package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfLifeRevealAndExileEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "205")
public class VizkopaConfessor extends Card {

    public VizkopaConfessor() {
        // Extort — whenever you cast a spell, you may pay {W/B} to drain each opponent for 1.
        addTrigger(Triggers.youCastSpell().anySpell(),
                new MayPayManaEffect("{W/B}", new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true), "Pay {W/B} to extort?"));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS), new PayAnyAmountOfLifeRevealAndExileEffect());
    }
}
