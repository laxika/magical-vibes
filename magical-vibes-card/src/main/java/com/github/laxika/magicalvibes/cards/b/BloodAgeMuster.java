package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

import java.util.List;

@CardRegistration(set = "YSOS", collectorNumber = "16")
public class BloodAgeMuster extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Blood Age General",
            "Charging Strifeknight",
            "Fuming Effigy",
            "Pillardrop Warden",
            "Spirit Mascot",
            "Stone Docent",
            "Stonebinder's Familiar",
            "Stonebound Mentor",
            "Stonerise Spirit",
            "Summoned Dromedary");

    public BloodAgeMuster() {
        // Whenever one or more cards leave your graveyard, conjure a random card from this
        // enchantment's spellbook onto the battlefield. Its base power and toughness perpetually
        // become 2/2. This ability triggers only once each turn.
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD,
                new OncePerTurnTriggerEffect(
                        new ConjureRandomCardFromSpellbookToBattlefieldEffect(SPELLBOOK, 2, 2)));
    }
}
