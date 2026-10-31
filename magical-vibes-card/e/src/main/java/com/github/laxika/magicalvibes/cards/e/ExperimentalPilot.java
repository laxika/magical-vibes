package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndSaddleEffect;

import java.util.List;

@CardRegistration(set = "YNEO", collectorNumber = "6")
public class ExperimentalPilot extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "203"),
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "198"),
            new DraftFromSpellbookEffect.SpellbookCard("KHM", "242"),
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "206"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "54"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "249"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "255"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "247"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "62"),
            new DraftFromSpellbookEffect.SpellbookCard("WAR", "66"),
            new DraftFromSpellbookEffect.SpellbookCard("AER", "165"),
            new DraftFromSpellbookEffect.SpellbookCard("AER", "179"),
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "225"),
            new DraftFromSpellbookEffect.SpellbookCard("AER", "149"),
            new DraftFromSpellbookEffect.SpellbookCard("THB", "239"));

    public ExperimentalPilot() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new DiscardCardTypeCost(null, null, 2),
                        new DraftCardFromSpellbookEffect(SPELLBOOK)),
                "{U}, Discard two cards: Draft a card from Experimental Pilot's spellbook."
        ));

        addStaticEffect(new GrantEffectEffect(
                new PowerBoostForCrewAndSaddleEffect(2, true), GrantScope.SELF));
    }
}
