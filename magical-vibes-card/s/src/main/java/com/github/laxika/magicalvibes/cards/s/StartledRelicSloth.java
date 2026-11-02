package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "233")
public class StartledRelicSloth extends Card {

    public StartledRelicSloth() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                ExileGraveyardCardsEffect.upToOneTargetFromAnyGraveyard());
    }
}
