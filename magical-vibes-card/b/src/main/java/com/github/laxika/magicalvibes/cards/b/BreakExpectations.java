package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardRecipient;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.RevealMatchingCardsFromTargetHandAndExileEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "26")
public class BreakExpectations extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "236"),
            new DraftFromSpellbookEffect.SpellbookCard("ATQ", "56"),
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "239"),
            new DraftFromSpellbookEffect.SpellbookCard("MMQ", "304"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "244"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "242"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "236"),
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "247"),
            new DraftFromSpellbookEffect.SpellbookCard("RIX", "181"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "244"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "241"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "252"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "248"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "251"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "219"));

    public BreakExpectations() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player."))
                .addSpellEffect(new RevealMatchingCardsFromTargetHandAndExileEffect(
                                new CardMinManaValuePredicate(2),
                                new DraftCardFromSpellbookEffect(
                                        SPELLBOOK, DraftCardRecipient.TARGET_PLAYER, true)));
    }
}
