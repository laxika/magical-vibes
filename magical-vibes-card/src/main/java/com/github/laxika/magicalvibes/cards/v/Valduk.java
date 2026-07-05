package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "148")
public class Valduk extends Card {

    public Valduk() {
        // At the beginning of combat on your turn, for each Aura and Equipment attached to
        // Valduk, create a 3/1 red Elemental creature token with trample and haste.
        // Exile those tokens at the beginning of the next end step.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new CreateTokenEffect(
                CardType.CREATURE,
                new AttachmentsOnSource(true, true),
                "Elemental",
                3,
                1,
                CardColor.RED,
                null,
                List.of(CardSubtype.ELEMENTAL),
                Set.of(Keyword.TRAMPLE, Keyword.HASTE),
                Set.of(),
                false,  // tappedAndAttacking
                false,  // tapped
                Map.of(),
                List.of(),
                false,  // exileAtEndOfCombat
                true,   // exileAtEndStep
                false,  // legendary
                0,
                Set.of()
        ));
    }
}
