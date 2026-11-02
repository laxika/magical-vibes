package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "127")
public class GalvanicRelay extends Card {

    public GalvanicRelay() {
        addSpellEffect(ExileTopCardsMayPlayUntilNextTurnEffect.duringOwnersNextTurn(1));
        addTrigger(Triggers.youCastThisSpell(), new StormEffect());
    }
}
