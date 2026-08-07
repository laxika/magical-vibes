package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureDrawPowerGainToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "ORI", collectorNumber = "191")
public class NissasRevelation extends Card {

    public NissasRevelation() {
        // Scry 5, then reveal the top card of your library. If it's a creature card, you draw
        // cards equal to its power and you gain life equal to its toughness.
        addEffect(EffectSlot.SPELL, new ScryEffect(5));
        addEffect(EffectSlot.SPELL, new RevealTopCardCreatureDrawPowerGainToughnessEffect());
    }
}
