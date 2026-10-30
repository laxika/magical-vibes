package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "22")
public class EmmaraVoiceOfTheConclave extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("MOM", "174"),
            new DraftFromSpellbookEffect.SpellbookCard("GRN", "6"),
            new DraftFromSpellbookEffect.SpellbookCard("MOM", "26"),
            new DraftFromSpellbookEffect.SpellbookCard("GRN", "20"),
            new DraftFromSpellbookEffect.SpellbookCard("GRN", "188"),
            new DraftFromSpellbookEffect.SpellbookCard("M15", "188"),
            new DraftFromSpellbookEffect.SpellbookCard("RAV", "175"),
            new DraftFromSpellbookEffect.SpellbookCard("M15", "40"),
            new DraftFromSpellbookEffect.SpellbookCard("GRN", "30"));

    public EmmaraVoiceOfTheConclave() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DraftCardFromSpellbookEffect(SPELLBOOK));
    }
}
