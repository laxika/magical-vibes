package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AER", collectorNumber = "5")
@CardRegistration(set = "KLR", collectorNumber = "5")
public class AirdropAeronauts extends Card {

    public AirdropAeronauts() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(new PermanentLeftBattlefieldUnderYourControlThisTurn(),
                        new GainLifeEffect(5)));
    }
}
