package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "23")
public class TirelessAngler extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("XLN", "57"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "67"),
            new DraftFromSpellbookEffect.SpellbookCard("M21", "326"),
            new DraftFromSpellbookEffect.SpellbookCard("THB", "55"),
            new DraftFromSpellbookEffect.SpellbookCard("IKO", "64"),
            new DraftFromSpellbookEffect.SpellbookCard("IKO", "66"),
            new DraftFromSpellbookEffect.SpellbookCard("M21", "72"),
            new DraftFromSpellbookEffect.SpellbookCard("THB", "69"),
            new DraftFromSpellbookEffect.SpellbookCard("IKO", "70"),
            new DraftFromSpellbookEffect.SpellbookCard("IKO", "41"),
            new DraftFromSpellbookEffect.SpellbookCard("THB", "291"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "62"),
            new DraftFromSpellbookEffect.SpellbookCard("JOU", "52"),
            new DraftFromSpellbookEffect.SpellbookCard("THB", "61"),
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "75"));

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
