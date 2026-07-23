package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventTargetCreatureRegenerationThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;

@CardRegistration(set = "ICE", collectorNumber = "129")
public class Gravebind extends Card {

    public Gravebind() {
        // "Target creature can't be regenerated this turn."
        addEffect(EffectSlot.SPELL, new PreventTargetCreatureRegenerationThisTurnEffect());
        // "Draw a card at the beginning of the next turn's upkeep."
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
