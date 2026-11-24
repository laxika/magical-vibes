package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "26")
public class SlimefootThallidTransplant extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "84"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "181"),
            new DraftFromSpellbookEffect.SpellbookCard("GRN", "196"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "222"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "181"),
            new DraftFromSpellbookEffect.SpellbookCard("M14", "196"),
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "207"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "106"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "107"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "189"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "160"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "187"),
            new DraftFromSpellbookEffect.SpellbookCard("TSP", "232"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "180"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "178"));

    public SlimefootThallidTransplant() {
        // Whenever a Swamp or Forest enters under your control, draft a card from this creature's
        // spellbook.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.LAND,
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                                        new PermanentHasSubtypePredicate(CardSubtype.FOREST))))),
                        ObjectRelation.YOU_CONTROL),
                new DraftCardFromSpellbookEffect(SPELLBOOK));
    }
}
