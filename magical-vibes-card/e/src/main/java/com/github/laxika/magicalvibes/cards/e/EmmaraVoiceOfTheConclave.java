package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "22")
public class EmmaraVoiceOfTheConclave extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Ancient Imperiosaur",
            "Conclave Tribunal",
            "Knight-Errant of Eos",
            "Loxodon Restorer",
            "March of the Multitudes",
            "Nissa's Expedition",
            "Overwhelm",
            "Triplicate Spirits",
            "Venerated Loxodon");

    public EmmaraVoiceOfTheConclave() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DraftCardFromSpellbookEffect(SPELLBOOK));
    }
}
