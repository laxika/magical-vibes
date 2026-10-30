package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;

import java.util.List;

@CardRegistration(set = "YNEO", collectorNumber = "26")
public class BoseijuPathlighter extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("XLN", "254"),
            new DraftFromSpellbookEffect.SpellbookCard("IKO", "245"),
            new DraftFromSpellbookEffect.SpellbookCard("M15", "245"),
            new DraftFromSpellbookEffect.SpellbookCard("AFC", "268"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "261"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "245"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "245"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "266"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "275"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "272"),
            new DraftFromSpellbookEffect.SpellbookCard("HOU", "182"),
            new DraftFromSpellbookEffect.SpellbookCard("WAR", "245"),
            new DraftFromSpellbookEffect.SpellbookCard("WWK", "138"),
            new DraftFromSpellbookEffect.SpellbookCard("WAR", "249"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "267"));

    public BoseijuPathlighter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DraftCardFromSpellbookEffect(SPELLBOOK));
    }
}
