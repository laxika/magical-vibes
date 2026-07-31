package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;

@CardRegistration(set = "ALL", collectorNumber = "50a")
@CardRegistration(set = "ALL", collectorNumber = "50b")
public class FeveredStrength extends Card {

    public FeveredStrength() {
        // "Target creature gets +2/+0 until end of turn."
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 0));
        // "Draw a card at the beginning of the next turn's upkeep."
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
