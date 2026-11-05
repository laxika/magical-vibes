package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YNEO", collectorNumber = "30")
public class ImperialBlademaster extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "2"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "131"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "1"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "215"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "245"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "10"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "217"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "146"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "19"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "31"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "156"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "158"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "35"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "39"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "165"));

    public ImperialBlademaster() {
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.SAMURAI),
                                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)))),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                new DraftCardFromSpellbookEffect(SPELLBOOK));
    }
}
