package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "131")
public class IronFistPulverizer extends Card {

    public IronFistPulverizer() {
        // Whenever you cast your second spell each turn, this creature deals 2 damage to target
        // opponent. Scry 1.
        PlayerPredicateTargetFilter opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        );
        target(opponent).addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER),
                new ScryEffect(1));
    }
}
