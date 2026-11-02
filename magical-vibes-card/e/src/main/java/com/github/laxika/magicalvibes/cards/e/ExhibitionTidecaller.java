package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "48")
@CardRegistration(set = "SOS", collectorNumber = "316")
public class ExhibitionTidecaller extends Card {

    public ExhibitionTidecaller() {
        // Opus — Whenever you cast an instant or sorcery spell, target player mills three cards.
        // If five or more mana was spent to cast that spell, that player mills ten cards instead.
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )),
                List.of(new ConditionalReplacementEffect(
                        new SpellManaSpentAtLeast(5),
                        new MillEffect(3, MillRecipient.TARGET_PLAYER),
                        new MillEffect(10, MillRecipient.TARGET_PLAYER)
                ))
        ));
    }
}
