package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "7")
public class FaithfulDisciple extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Anointed Procession",
            "Cathars' Crusade",
            "Authority of the Consuls",
            "Sigil of the Empty Throne",
            "All That Glitters",
            "Banishing Light",
            "Divine Visitation",
            "Duelist's Heritage",
            "Glorious Anthem",
            "Gauntlets of Light",
            "Teleportation Circle",
            "Angelic Gift",
            "Spectral Steel",
            "Cleric Class",
            "Angelic Exaltation");

    public FaithfulDisciple() {
        addEffect(EffectSlot.ON_DEATH, new DraftCardFromSpellbookEffect(SPELLBOOK));
    }
}
