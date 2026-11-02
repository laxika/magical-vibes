package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "36")
@CardRegistration(set = "MKM", collectorNumber = "340")
@CardRegistration(set = "MKM", collectorNumber = "429")
@CardRegistration(set = "FDC", collectorNumber = "57")
public class WojekInvestigator extends Card {

    public WojekInvestigator() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                CreateTokenEffect.ofClueToken(new OpponentsWithMoreCardsInHandThanController()));
    }
}
