package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscordRandomCardCopyEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "798")
public class DiscordLordOfDisharmony extends Card {

    public DiscordLordOfDisharmony() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new DiscordRandomCardCopyEffect());
    }
}
