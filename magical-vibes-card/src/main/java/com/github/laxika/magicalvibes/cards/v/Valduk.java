package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenPerAttachmentOnSourceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "148")
public class Valduk extends Card {

    public Valduk() {
        // At the beginning of combat on your turn, for each Aura and Equipment attached to
        // Valduk, create a 3/1 red Elemental creature token with trample and haste.
        // Exile those tokens at the beginning of the next end step.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new CreateTokenPerAttachmentOnSourceEffect(
                "Elemental",
                3,
                1,
                CardColor.RED,
                List.of(CardSubtype.ELEMENTAL),
                Set.of(Keyword.TRAMPLE, Keyword.HASTE),
                Set.of(),
                true,   // countAuras
                true,   // countEquipment
                true    // exileAtEndStep
        ));
    }
}
