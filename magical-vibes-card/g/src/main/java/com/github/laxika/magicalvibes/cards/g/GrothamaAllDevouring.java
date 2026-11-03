package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.DamageDealtToSourceByControllerThisTurn;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLC", collectorNumber = "224")
public class GrothamaAllDevouring extends Card {

    public GrothamaAllDevouring() {
        addTrigger(Triggers.creatureAttacks()
                        .withFilter(new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())),
                new MayEffect(
                        new SourceFightsTargetCreatureEffect(),
                        "Have it fight Grothama, All-Devouring?",
                        null,
                        MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new EachPlayerDrawsCardEffect(new DamageDealtToSourceByControllerThisTurn()));
    }
}
