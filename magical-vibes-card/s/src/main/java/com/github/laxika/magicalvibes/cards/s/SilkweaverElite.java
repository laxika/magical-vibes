package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AER", collectorNumber = "125")
public class SilkweaverElite extends Card {

    public SilkweaverElite() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(new PermanentLeftBattlefieldUnderYourControlThisTurn(),
                        new DrawCardEffect()));
    }
}
