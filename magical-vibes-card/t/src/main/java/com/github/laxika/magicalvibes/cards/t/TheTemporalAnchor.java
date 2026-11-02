package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileBottomCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "82")
public class TheTemporalAnchor extends Card {

    public TheTemporalAnchor() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ScryEffect(2));
        addEffect(EffectSlot.ON_CONTROLLER_SCRIES, new ExileBottomCardsToSourceEffect(new EventValue()));
        addStaticEffect(new AllowCastFromCardsExiledWithSourceEffect(
                false, null, false, true, 0, null, false, false, false));
    }
}
