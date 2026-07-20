package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReduceGraveyardCardActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "227")
public class EmbalmersTools extends Card {

    public EmbalmersTools() {
        // Activated abilities of creature cards in your graveyard cost {1} less to activate.
        addEffect(EffectSlot.STATIC, new ReduceGraveyardCardActivatedAbilityCostEffect(
                new CardTypePredicate(CardType.CREATURE), 1));

        // Tap an untapped Zombie you control: Target player mills a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new TapMultiplePermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)),
                        new MillEffect(1, MillRecipient.TARGET_PLAYER)),
                "Tap an untapped Zombie you control: Target player mills a card."
        ));
    }
}
