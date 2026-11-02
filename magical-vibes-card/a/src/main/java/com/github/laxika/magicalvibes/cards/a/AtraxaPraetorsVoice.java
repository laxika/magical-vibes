package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "453")
@CardRegistration(set = "2XM", collectorNumber = "190")
@CardRegistration(set = "MUL", collectorNumber = "33")
@CardRegistration(set = "MUL", collectorNumber = "98")
@CardRegistration(set = "MUL", collectorNumber = "163")
@CardRegistration(set = "C16", collectorNumber = "28")
@CardRegistration(set = "CM2", collectorNumber = "10")
public class AtraxaPraetorsVoice extends Card {

    public AtraxaPraetorsVoice() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ProliferateEffect());
    }
}
