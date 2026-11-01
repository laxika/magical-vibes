package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOneFromTopCardsFaceDownWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "76")
public class WatcherForTomorrow extends Card {

    public WatcherForTomorrow() {
        addStaticEffect(new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileOneFromTopCardsFaceDownWithSourceEffect(4, true));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new PutAllCardsExiledWithSourceIntoOwnersHandsEffect());
    }
}
