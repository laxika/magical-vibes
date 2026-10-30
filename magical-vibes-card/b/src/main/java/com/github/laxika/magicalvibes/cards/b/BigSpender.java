package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "YSNC", collectorNumber = "10")
public class BigSpender extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("MID", "259"),
            new DraftFromSpellbookEffect.SpellbookCard("AER", "149"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "257"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "261"),
            new DraftFromSpellbookEffect.SpellbookCard("MRD", "175"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "222"),
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "220"),
            new DraftFromSpellbookEffect.SpellbookCard("ROE", "222"),
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "212"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "220"),
            new DraftFromSpellbookEffect.SpellbookCard("M19", "235"),
            new DraftFromSpellbookEffect.SpellbookCard("WAR", "239"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "261"),
            new DraftFromSpellbookEffect.SpellbookCard("M19", "227"),
            new DraftFromSpellbookEffect.SpellbookCard("M19", "231"));

    public BigSpender() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_BECOME_BLOCKED, CreateTokenEffect.ofTreasureToken(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentIsArtifactPredicate()),
                        new DraftCardFromSpellbookEffect(SPELLBOOK)
                ),
                "Sacrifice two artifacts: Draft a card from Big Spender's spellbook."
        ));
    }
}
