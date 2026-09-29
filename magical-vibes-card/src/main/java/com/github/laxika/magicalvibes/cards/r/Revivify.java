package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.CreatureCardsInGraveyardFromBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "AFC", collectorNumber = "10")
public class Revivify extends Card {

    public Revivify() {
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        ReturnCardFromGraveyardEffect returnToHand = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(creature)
                .returnAll(true)
                .thisTurnOnly(true)
                .build();
        ReturnCardFromGraveyardEffect returnToBattlefield = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(creature)
                .returnAll(true)
                .thisTurnOnly(true)
                .build();

        addEffect(EffectSlot.SPELL, RollD20Effect.withAddedAmount(
                new CreatureCardsInGraveyardFromBattlefieldThisTurn(), returnToHand, returnToBattlefield));
    }
}
