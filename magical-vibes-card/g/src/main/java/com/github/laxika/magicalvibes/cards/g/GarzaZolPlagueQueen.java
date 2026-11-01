package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "129")
public class GarzaZolPlagueQueen extends Card {

    public GarzaZolPlagueQueen() {
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS), new PutCountersOnSourceEffect(1, 1, 1));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new DrawCardEffect(1), "Draw a card?"));
    }
}
