package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookToExileEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoHandEffect;

import java.util.List;

@CardRegistration(set = "YSOS", collectorNumber = "30")
public class TheMysticalArchive extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Abrade", "Ad Nauseam", "Armageddon", "Berserk", "Big Score", "Bulk Up",
            "Crackle with Power", "Deduce", "Disdainful Stroke", "Empty the Warrens",
            "Force of Will", "Giant Growth", "Hop to It", "Knockout Maneuver", "Pick Your Poison",
            "Pongify", "Prismatic Ending", "Reprieve", "Requisition Raid", "Shamanic Revelation",
            "Sheoldred's Edict", "Smallpox", "Stargaze", "Stock Up", "Zombify");

    public TheMysticalArchive() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DraftCardFromSpellbookToExileEffect(SPELLBOOK));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(true, "{1}",
                List.of(new AwardAnyColorManaEffect(2,
                        ManaSpendRestriction.OUTSIDE_STARTING_DECK_SPELL_ONLY, true)),
                "{1}, {T}: Add two mana in any combination of colors. Spend this mana only to cast spells that aren't from your starting deck."));
        addActivatedAbility(new ActivatedAbility(true, "{2}",
                List.of(new PutCardExiledWithSourceIntoHandEffect()),
                "{2}, {T}: Put a card exiled with this land into its owner's hand."));
    }
}
