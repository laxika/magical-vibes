package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

import java.util.List;

/**
 * Conqueror's Foothold — back face of Conqueror's Galleon.
 * Land.
 * {T}: Add {C}.
 * {2}, {T}: Draw a card, then discard a card.
 * {4}, {T}: Draw a card.
 * {6}, {T}: Return target card from your graveyard to your hand.
 */
public class ConquerorsFoothold extends Card {

    public ConquerorsFoothold() {
        // {T}: Add {C}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS));

        // {2}, {T}: Draw a card, then discard a card.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}",
                List.of(new DrawAndDiscardCardEffect()),
                "{2}, {T}: Draw a card, then discard a card."
        ));

        // {4}, {T}: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true, "{4}",
                List.of(new DrawCardEffect()),
                "{4}, {T}: Draw a card."
        ));

        // {6}, {T}: Return target card from your graveyard to your hand.
        addActivatedAbility(new ActivatedAbility(
                true, "{6}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .targetGraveyard(true)
                        .build()),
                "{6}, {T}: Return target card from your graveyard to your hand."
        ));
    }
}
