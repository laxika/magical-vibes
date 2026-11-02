package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "98")
@CardRegistration(set = "DDS", collectorNumber = "13")
public class TheUnspeakable extends Card {

    public TheUnspeakable() {
        // Whenever The Unspeakable deals combat damage to a player, you may return target
        // Arcane card from your graveyard to your hand. Choose the target as the trigger
        // is put on the stack, then choose whether to return it on resolution.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardSubtypePredicate(CardSubtype.ARCANE))
                .targetGraveyard(true)
                .build(), "Return target Arcane card to your hand?"));
    }
}
