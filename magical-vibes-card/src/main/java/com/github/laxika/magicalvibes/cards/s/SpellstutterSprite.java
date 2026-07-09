package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueAtMostControlledCountPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "89")
public class SpellstutterSprite extends Card {

    public SpellstutterSprite() {
        // Flash, Flying — auto-loaded from Scryfall.
        //
        // When this creature enters, counter target spell with mana value X or less, where X is
        // the number of Faeries you control. Spellstutter Sprite is a Faerie already on the
        // battlefield when the ETB triggers, so it counts itself. The legal-target restriction is
        // evaluated as the ETB ability is put onto the stack via the ETB spell-target pipeline.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryManaValueAtMostControlledCountPredicate(
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.FAERIE))),
                "Target spell's mana value must be at most the number of Faeries you control."
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CounterSpellEffect());
    }
}
