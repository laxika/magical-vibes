package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToExileEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "YSNC", collectorNumber = "1")
public class CelestialVault extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "2"),
            new DraftFromSpellbookEffect.SpellbookCard("M19", "34"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "4"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "24"),
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "4"),
            new DraftFromSpellbookEffect.SpellbookCard("AKH", "1"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "374"),
            new DraftFromSpellbookEffect.SpellbookCard("AVR", "19"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "382"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "22"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "17"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "34"),
            new DraftFromSpellbookEffect.SpellbookCard("LEA", "39"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "31"),
            new DraftFromSpellbookEffect.SpellbookCard("MH1", "25"));

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
