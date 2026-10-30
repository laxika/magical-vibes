package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToExileEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoHandEffect;

import java.util.List;

@CardRegistration(set = "YSOS", collectorNumber = "30")
public class TheMysticalArchive extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("HOU", "83"),
            new DraftFromSpellbookEffect.SpellbookCard("ALA", "63"),
            new DraftFromSpellbookEffect.SpellbookCard("LEA", "2"),
            new DraftFromSpellbookEffect.SpellbookCard("LEA", "185"),
            new DraftFromSpellbookEffect.SpellbookCard("SNC", "102"),
            new DraftFromSpellbookEffect.SpellbookCard("FDN", "80"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "95"),
            new DraftFromSpellbookEffect.SpellbookCard("MKM", "52"),
            new DraftFromSpellbookEffect.SpellbookCard("KTK", "37"),
            new DraftFromSpellbookEffect.SpellbookCard("TSP", "152"),
            new DraftFromSpellbookEffect.SpellbookCard("ALL", "28"),
            new DraftFromSpellbookEffect.SpellbookCard("LEA", "197"),
            new DraftFromSpellbookEffect.SpellbookCard("BLB", "16"),
            new DraftFromSpellbookEffect.SpellbookCard("TDM", "147"),
            new DraftFromSpellbookEffect.SpellbookCard("MKM", "170"),
            new DraftFromSpellbookEffect.SpellbookCard("PLC", "44"),
            new DraftFromSpellbookEffect.SpellbookCard("MH2", "25"),
            new DraftFromSpellbookEffect.SpellbookCard("OMB", "5"),
            new DraftFromSpellbookEffect.SpellbookCard("OTJ", "26"),
            new DraftFromSpellbookEffect.SpellbookCard("FRF", "138"),
            new DraftFromSpellbookEffect.SpellbookCard("ONE", "108"),
            new DraftFromSpellbookEffect.SpellbookCard("TSP", "131"),
            new DraftFromSpellbookEffect.SpellbookCard("BLB", "114"),
            new DraftFromSpellbookEffect.SpellbookCard("DFT", "67"),
            new DraftFromSpellbookEffect.SpellbookCard("ODY", "171"));

    public TheMysticalArchive() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DraftCardFromSpellbookToExileEffect(SPELLBOOK));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(true, "{1}",
                List.of(new AwardAnyColorManaEffect(2,
                        ManaSpendRestriction.OUTSIDE_STARTING_DECK_SPELL_ONLY, true)),
                "{1}, {T}: Add two mana in any combination of colors. Spend this mana only to cast spells that aren't from your starting deck."));
        addActivatedAbility(new ActivatedAbility(true, "{2}",
                List.of(PutCardExiledWithSourceIntoHandEffect.returningToOwner()),
                "{2}, {T}: Put a card exiled with this land into its owner's hand."));
    }
}
