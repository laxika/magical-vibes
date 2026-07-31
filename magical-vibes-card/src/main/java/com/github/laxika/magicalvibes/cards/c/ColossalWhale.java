package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M14", collectorNumber = "48")
public class ColossalWhale extends Card {

    public ColossalWhale() {
        // Whenever this creature attacks, you may exile target creature defending player
        // controls until this creature leaves the battlefield. The defending player is the
        // attacked opponent, matched by the opponent-controlled creature filter (Master of Diversion).
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                        new ExileTargetPermanentUntilSourceLeavesEffect(),
                        "Exile target creature defending player controls until Colossal Whale leaves the battlefield?"));
    }
}
