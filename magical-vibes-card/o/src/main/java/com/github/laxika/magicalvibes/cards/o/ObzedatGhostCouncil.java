package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfReturnAtNextUpkeepWithHasteEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "182")
@CardRegistration(set = "MM3", collectorNumber = "176")
@CardRegistration(set = "EA3", collectorNumber = "16")
public class ObzedatGhostCouncil extends Card {

    public ObzedatGhostCouncil() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(2));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new MayEffect(new ExileSelfReturnAtNextUpkeepWithHasteEffect(),
                        "Exile Obzedat, Ghost Council? It returns at the beginning of your next upkeep with haste."));
    }
}
