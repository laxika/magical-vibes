package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PeaceTalksEffect;

@CardRegistration(set = "VIS", collectorNumber = "15")
@CardRegistration(set = "MGB", collectorNumber = "1")
public class PeaceTalks extends Card {

    public PeaceTalks() {
        // This turn and next turn, creatures can't attack, and players and permanents can't be the
        // targets of spells or activated abilities.
        addEffect(EffectSlot.SPELL, new PeaceTalksEffect());
    }
}
