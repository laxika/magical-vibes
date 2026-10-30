package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "24")
public class CurseboundWitch extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("M21", "129"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "249"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "81"),
            new DraftFromSpellbookEffect.SpellbookCard("M13", "82"),
            new DraftFromSpellbookEffect.SpellbookCard("AKH", "84"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "111"),
            new DraftFromSpellbookEffect.SpellbookCard("M15", "123"),
            new DraftFromSpellbookEffect.SpellbookCard("DKA", "54"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "90"),
            new DraftFromSpellbookEffect.SpellbookCard("HOU", "78"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "237"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "94"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "232"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "2"),
            new DraftFromSpellbookEffect.SpellbookCard("AKH", "112"));

    public CurseboundWitch() {
        addEffect(EffectSlot.ON_DEATH, new DraftCardFromSpellbookEffect(SPELLBOOK));
    }
}
