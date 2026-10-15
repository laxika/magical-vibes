package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;

import java.util.List;

@CardRegistration(set = "YNEO", collectorNumber = "26")
public class BoseijuPathlighter extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Field of Ruin",
            "Bonders' Enclave",
            "Radiant Fountain",
            "Thriving Grove",
            "Treasure Vault",
            "Gingerbread Cabin",
            "Memorial to Unity",
            "Boseiju, Who Endures",
            "Secluded Courtyard",
            "Roadside Reliquary",
            "Scavenger Grounds",
            "Emergence Zone",
            "Khalni Garden",
            "Mobilized District",
            "Hall of Oracles");

    public BoseijuPathlighter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DraftCardFromSpellbookEffect(SPELLBOOK));
    }
}
