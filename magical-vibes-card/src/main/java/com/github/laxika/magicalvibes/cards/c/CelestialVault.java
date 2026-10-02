package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToExileEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "YSNC", collectorNumber = "1")
public class CelestialVault extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Angel of Destiny",
            "Resplendent Angel",
            "Angel of Vitality",
            "Righteous Valkyrie",
            "Angel of Invention",
            "Angel of Sanctions",
            "Valkyrie Harbinger",
            "Emancipation Angel",
            "Youthful Valkyrie",
            "Resplendent Marshal",
            "Enduring Angel",
            "Sigardian Savior",
            "Serra Angel",
            "Stalwart Valkyrie",
            "Segovian Angel");

    public CelestialVault() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new DraftCardFromSpellbookToExileEffect(SPELLBOOK)),
                "{W}, {T}: Draft a card from Celestial Vault's spellbook and exile it face down."));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new PutAllCardsExiledWithSourceIntoOwnersHandsEffect()),
                "{1}, Sacrifice Celestial Vault: Put each card exiled with Celestial Vault into your hand."));
    }
}
