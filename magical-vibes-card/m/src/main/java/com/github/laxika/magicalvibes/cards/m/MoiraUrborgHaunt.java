package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DMC", collectorNumber = "6")
@CardRegistration(set = "DMC", collectorNumber = "82")
public class MoiraUrborgHaunt extends Card {

    public MoiraUrborgHaunt() {
        // Whenever Moira deals combat damage to a player, return to the battlefield target creature
        // card in your graveyard that was put there from the battlefield this turn.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .targetPutIntoGraveyardFromBattlefieldThisTurn(true)
                        .build());
    }
}
