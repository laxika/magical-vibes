package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "51")
public class DiligentExcavator extends Card {

    public DiligentExcavator() {
        // Whenever you cast a historic spell, target player mills two cards.
        // (Artifacts, legendaries, and Sagas are historic.)
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addTrigger(Triggers.youCastSpell().withFilter(new CardIsHistoricPredicate()),
                new MillEffect(2, MillRecipient.TARGET_PLAYER));
    }
}
