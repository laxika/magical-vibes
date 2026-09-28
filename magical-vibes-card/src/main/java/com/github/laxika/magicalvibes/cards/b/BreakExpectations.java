package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardRecipient;
import com.github.laxika.magicalvibes.model.effect.RevealMatchingCardsFromTargetHandAndExileEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "26")
public class BreakExpectations extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Colossal Plow",
            "Millstone",
            "Whirlermaker",
            "Magistrate's Scepter",
            "Replicating Ring",
            "Raiders' Karve",
            "Weapon Rack",
            "Relic Amulet",
            "Orazca Relic",
            "Fifty Feet of Rope",
            "Pyre of Heroes",
            "Treasure Chest",
            "Leather Armor",
            "Spiked Pit Trap",
            "Gingerbrute");

    public BreakExpectations() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player."))
                .addEffect(EffectSlot.SPELL,
                        new RevealMatchingCardsFromTargetHandAndExileEffect(
                                new CardMinManaValuePredicate(2),
                                new DraftCardFromSpellbookEffect(
                                        SPELLBOOK, DraftCardRecipient.TARGET_PLAYER, true)));
    }
}
