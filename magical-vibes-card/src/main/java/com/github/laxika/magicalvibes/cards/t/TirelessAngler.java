package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "23")
public class TirelessAngler extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Fleet Swallower",
            "Moat Piranhas",
            "Mystic Skyfish",
            "Nadir Kraken",
            "Pouncing Shoreshark",
            "Sea-Dasher Octopus",
            "Spined Megalodon",
            "Stinging Lionfish",
            "Voracious Greatshark",
            "Archipelagore",
            "Serpent of Yawning Depths",
            "Wormhole Serpent",
            "Sigiled Starfish",
            "Riptide Turtle",
            "Ruin Crab");

    public TirelessAngler() {
        // Whenever an Island or Swamp enters the battlefield under your control, draft a card from
        // this creature's spellbook.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.ISLAND),
                                new CardSubtypePredicate(CardSubtype.SWAMP))),
                        new DraftCardFromSpellbookEffect(SPELLBOOK)));
    }
}
