package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromSpellbookToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YSOS", collectorNumber = "16")
public class BloodAgeMuster extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("STX", "93"),
            new DraftFromSpellbookEffect.SpellbookCard("SOS", "110"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "103"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "112"),
            new DraftFromSpellbookEffect.SpellbookCard("SOS", "230"),
            new DraftFromSpellbookEffect.SpellbookCard("SOS", "36"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "31"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "239"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "32"),
            new DraftFromSpellbookEffect.SpellbookCard("SOS", "37"));

    public BloodAgeMuster() {
        // Whenever one or more cards leave your graveyard, conjure a random card from this
        // enchantment's spellbook onto the battlefield. Its base power and toughness perpetually
        // become 2/2. This ability triggers only once each turn.
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new ConjureRandomCardFromSpellbookToBattlefieldEffect(SPELLBOOK, 2, 2));
    }
}
