package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "7")
public class FaithfulDisciple extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("AKH", "2"),
            new DraftFromSpellbookEffect.SpellbookCard("AVR", "10"),
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "5"),
            new DraftFromSpellbookEffect.SpellbookCard("CON", "18"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "2"),
            new DraftFromSpellbookEffect.SpellbookCard("JOU", "5"),
            new DraftFromSpellbookEffect.SpellbookCard("GRN", "10"),
            new DraftFromSpellbookEffect.SpellbookCard("C16", "1"),
            new DraftFromSpellbookEffect.SpellbookCard("USG", "15"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "17"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "39"),
            new DraftFromSpellbookEffect.SpellbookCard("BFZ", "19"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "30"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "6"),
            new DraftFromSpellbookEffect.SpellbookCard("RNA", "2"));

    public FaithfulDisciple() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DraftCardFromSpellbookEffect(SPELLBOOK));
    }
}
