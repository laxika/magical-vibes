package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "62")
public class OminousTraveler extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Dominating Vampire",
            "Vampire Socialite",
            "Stromkirk Bloodthief",
            "Falkenrath Pit Fighter",
            "Wolfkin Outcast",
            "Howlpack Piper",
            "Tovolar, Dire Overlord",
            "Patrician Geist",
            "Shipwreck Sifters",
            "Steelclad Spirit",
            "Heron-Blessed Geist",
            "Archghoul of Thraben",
            "Champion of the Perished",
            "Headless Rider",
            "Bladestitched Skaab");

    public OminousTraveler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DraftCardFromSpellbookEffect(SPELLBOOK, List.of(
                        new PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect(
                                new ReturnPermanentControlledByPlayerToHandEffect(
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentNamedPredicate("Ominous Traveler"))),
                                        "creature")))));
    }
}
