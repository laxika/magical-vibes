package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "BOK", collectorNumber = "47")
@CardRegistration(set = "UDS", collectorNumber = "42")
public class Quash extends Card {

    public Quash() {
        // Counter target instant or sorcery spell. Search its controller's graveyard, hand, and
        // library for all cards with the same name as that spell and exile them. Then that player
        // shuffles. Instant/sorcery-only sibling of Counterbore.
        addEffect(EffectSlot.SPELL, new CounterSpellAndExileAllWithSameNameEffect());
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                "Target must be an instant or sorcery spell."
        ));
    }
}
