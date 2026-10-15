package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "148")
@CardRegistration(set = "M11", collectorNumber = "150")
public class MagmaPhoenix extends Card {

    public MagmaPhoenix() {
        // When Magma Phoenix dies, it deals 3 damage to each creature and each player.
        addEffect(EffectSlot.ON_DEATH, new MassDamageEffect(3, true));

        // {3}{R}{R}: Return Magma Phoenix from your graveyard to your hand.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}{R}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{3}{R}{R}: Return Magma Phoenix from your graveyard to your hand."
        ));
    }
}
