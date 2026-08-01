package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EpicExperimentEffect;

@CardRegistration(set = "RTR", collectorNumber = "159")
public class EpicExperiment extends Card {

    public EpicExperiment() {
        // Exile the top X cards of your library. You may cast instant and sorcery spells with mana
        // value X or less from among them without paying their mana costs. Then put all cards
        // exiled this way that weren't cast into your graveyard.
        addEffect(EffectSlot.SPELL, new EpicExperimentEffect());
    }
}
