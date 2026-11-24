package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackingCreaturesOfSubtype;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "22")
public class RaddicTalZealot extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("GRN", "77"),
            new DraftFromSpellbookEffect.SpellbookCard("AFR", "18"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "105"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "10"),
            new DraftFromSpellbookEffect.SpellbookCard("M20", "94"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "6"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "97"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "99"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "1"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "14"),
            new DraftFromSpellbookEffect.SpellbookCard("M19", "42"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "105"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "79"),
            new DraftFromSpellbookEffect.SpellbookCard("DOM", "21"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "9"));

    public RaddicTalZealot() {
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackingCreaturesOfSubtype(1, CardSubtype.KNIGHT)),
                new DraftCardFromSpellbookEffect(SPELLBOOK));
    }
}
