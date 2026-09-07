package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;

@CardRegistration(set = "ELD", collectorNumber = "189")
public class EscapeToTheWilds extends Card {

    public EscapeToTheWilds() {
        addEffect(EffectSlot.SPELL, new ExileTopCardsMayPlayUntilNextTurnEffect(5));
        addEffect(EffectSlot.SPELL, new PlayAdditionalLandsEffect(1));
    }
}
