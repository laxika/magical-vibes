package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "67")
public class FollowTheTracks extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("HBG", "80"),
            new DraftFromSpellbookEffect.SpellbookCard("HBG", "79"),
            new DraftFromSpellbookEffect.SpellbookCard("HBG", "77"),
            new DraftFromSpellbookEffect.SpellbookCard("HBG", "81"),
            new DraftFromSpellbookEffect.SpellbookCard("HBG", "78"));

    public FollowTheTracks() {
        addSpellEffect(DraftCardFromSpellbookEffect.conjureToBattlefield(SPELLBOOK));
    }
}
