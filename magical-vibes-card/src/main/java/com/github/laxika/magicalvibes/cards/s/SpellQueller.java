package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "INR", collectorNumber = "248")
@CardRegistration(set = "INR", collectorNumber = "320")
@CardRegistration(set = "INR", collectorNumber = "435")
public class SpellQueller extends Card {

    public SpellQueller() {
        // Flash, Flying — auto-loaded from Scryfall.
        //
        // When this creature enters, exile target spell with mana value 4 or less. The mana value
        // restriction lives on the card's target filter, which the ETB spell-target pipeline reads
        // when the trigger goes on the stack (same as Spellstutter Sprite).
        target(new StackEntryPredicateTargetFilter(
                new StackEntryMaxManaValuePredicate(4),
                "Target spell's mana value must be 4 or less."
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTargetSpellUntilSourceLeavesEffect());

        // When this creature leaves the battlefield, the exiled card's owner may cast that card
        // without paying its mana cost.
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new MayCastCardsExiledWithSourceEffect());
    }
}
