package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "7")
public class ThoughtstealSprites extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("MOR", "58"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "39"),
            new DraftFromSpellbookEffect.SpellbookCard("FDN", "38"),
            new DraftFromSpellbookEffect.SpellbookCard("WOE", "90"),
            new DraftFromSpellbookEffect.SpellbookCard("MOM", "58"),
            new DraftFromSpellbookEffect.SpellbookCard("ECL", "48"),
            new DraftFromSpellbookEffect.SpellbookCard("ECL", "51"),
            new DraftFromSpellbookEffect.SpellbookCard("YWOE", "20"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "49"),
            new DraftFromSpellbookEffect.SpellbookCard("WOE", "208"),
            new DraftFromSpellbookEffect.SpellbookCard("LRW", "78"),
            new DraftFromSpellbookEffect.SpellbookCard("WOE", "64"),
            new DraftFromSpellbookEffect.SpellbookCard("WOE", "69"),
            new DraftFromSpellbookEffect.SpellbookCard("MOR", "55"),
            new DraftFromSpellbookEffect.SpellbookCard("ECL", "250"));

    public ThoughtstealSprites() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new OncePerTurnTriggerEffect(new SpellCastTriggerEffect(
                        null,
                        List.of(
                                new DraftFromSpellbookEffect(
                                        SPELLBOOK,
                                        DraftFromSpellbookEffect.DraftMode.CONJURE_TO_HAND),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                        true)));
    }
}
