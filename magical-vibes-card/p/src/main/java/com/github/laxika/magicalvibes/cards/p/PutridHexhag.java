package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YECL", collectorNumber = "10")
public class PutridHexhag extends Card {

    public PutridHexhag() {
        PermanentPredicate opponentCreature = new PermanentNotPredicate(
                new PermanentControlledBySourceControllerPredicate());
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.countersPutOnThis(),
                        new PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(
                                new TriggeredAbility(Triggers.dies(ObjectRelation.THIS),
                                new LoseLifeEffect(2)),
                                opponentCreature));
    }
}
