package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCounterToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YONE", collectorNumber = "9")
public class MarchTowardPerfection extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("ONE", "82"),
            new DraftFromSpellbookEffect.SpellbookCard("ONE", "83"),
            new DraftFromSpellbookEffect.SpellbookCard("YDMU", "9"),
            new DraftFromSpellbookEffect.SpellbookCard("NPH", "59"),
            new DraftFromSpellbookEffect.SpellbookCard("ONE", "234"),
            new DraftFromSpellbookEffect.SpellbookCard("BRO", "121"),
            new DraftFromSpellbookEffect.SpellbookCard("APC", "48"),
            new DraftFromSpellbookEffect.SpellbookCard("NPH", "68"),
            new DraftFromSpellbookEffect.SpellbookCard("APC", "49"),
            new DraftFromSpellbookEffect.SpellbookCard("MBS", "122"),
            new DraftFromSpellbookEffect.SpellbookCard("BRO", "123"),
            new DraftFromSpellbookEffect.SpellbookCard("ONE", "241"),
            new DraftFromSpellbookEffect.SpellbookCard("DMU", "112"),
            new DraftFromSpellbookEffect.SpellbookCard("NPH", "76"),
            new DraftFromSpellbookEffect.SpellbookCard("ONE", "246"));

    public MarchTowardPerfection() {
        addSpellEffect(RegisterDelayedControllerSpellCastTriggerEffect.oneShotUntilConsumed(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(CardSubtype.PHYREXIAN))),
                        List.of(
                                new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1),
                                new GrantAdditionalCounterToTriggeringCreatureSpellEffect(
                                        CounterType.DEATHTOUCH, 1))));
        addSpellEffect(new DraftCardFromSpellbookEffect(SPELLBOOK));
    }
}
