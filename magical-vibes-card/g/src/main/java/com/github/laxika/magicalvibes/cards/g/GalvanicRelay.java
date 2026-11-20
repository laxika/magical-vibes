package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "127")
public class GalvanicRelay extends Card {

    public GalvanicRelay() {
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(1, false).until(Duration.DURING_YOUR_NEXT_TURN));
        addTrigger(Triggers.youCastThisSpell(), CopyTriggeringSpellEffect.storm());
    }
}
