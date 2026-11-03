package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDSK", collectorNumber = "8")
public class GlimmerHoarder extends Card {

    public GlimmerHoarder() {
        DraftFromSpellbookEffect spellbook = new DraftFromSpellbookEffect(List.of(
                new DraftFromSpellbookEffect.SpellbookCard("SNC", "67"),
                new DraftFromSpellbookEffect.SpellbookCard("THB", "84"),
                new DraftFromSpellbookEffect.SpellbookCard("DSK", "84"),
                new DraftFromSpellbookEffect.SpellbookCard("YLCI", "9"),
                new DraftFromSpellbookEffect.SpellbookCard("DMU", "90"),
                new DraftFromSpellbookEffect.SpellbookCard("THB", "98"),
                new DraftFromSpellbookEffect.SpellbookCard("THB", "276"),
                new DraftFromSpellbookEffect.SpellbookCard("YMID", "31"),
                new DraftFromSpellbookEffect.SpellbookCard("LCI", "123")),
                DraftFromSpellbookEffect.DraftMode.PERPETUALLY_BECOMES_ENCHANTMENT);
        addTrigger(Triggers.secondMainPhase(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped()),
                SequenceEffect.of(new LoseLifeEffect(1), spellbook));
    }
}
