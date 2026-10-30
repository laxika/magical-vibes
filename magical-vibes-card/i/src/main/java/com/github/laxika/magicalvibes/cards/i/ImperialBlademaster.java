package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

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
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringCardConditionalEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SAMURAI),
                                new CardSubtypePredicate(CardSubtype.WARRIOR))),
                        new ConditionalEffect(new AttacksAlone(),
                                new DraftCardFromSpellbookEffect(SPELLBOOK))));
    }
}
