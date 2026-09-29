package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "67")
public class FollowTheTracks extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Gate to the Citadel",
            "Gate to Seatower",
            "Gate of the Black Dragon",
            "Gate to Tumbledown",
            "Gate to Manorborn");

    public FollowTheTracks() {
        addEffect(EffectSlot.SPELL, new DraftCardFromSpellbookEffect(SPELLBOOK, false, true));
    }
}
