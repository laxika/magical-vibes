package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CreaturesDiedThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "64")
@CardRegistration(set = "KHM", collectorNumber = "304")
@CardRegistration(set = "MUL", collectorNumber = "10")
@CardRegistration(set = "MUL", collectorNumber = "75")
@CardRegistration(set = "MUL", collectorNumber = "140")
@CardRegistration(set = "CMM", collectorNumber = "100")
public class IngaRuneEyes extends Card {

    public IngaRuneEyes() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(3));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ConditionalEffect(
                new CreaturesDiedThisTurnAtLeast(3), new DrawCardEffect(3)));
    }
}
