package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "EVE", collectorNumber = "123")
public class HagHedgeMage extends Card {

    public HagHedgeMage() {
        // When this creature enters, if you control two or more Swamps, you may have target player
        // discard a card. Intervening-if gate (CR 603.4): checked as the trigger goes on the stack
        // and again at resolution; the target player is chosen at trigger time (CR 603.3d).
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.SWAMP)),
                new MayEffect(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                        "Have target player discard a card?")));

        // When this creature enters, if you control two or more Forests, you may put target card
        // from your graveyard on top of your library. Modelled as a resolution-time choose from the
        // controller's own graveyard (the ETB pipeline has no graveyard-target selector for this
        // effect) — functionally identical since it only ever draws from your own graveyard.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                new MayEffect(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY)
                        .build(),
                        "Put target card from your graveyard on top of your library?")));
    }
}
