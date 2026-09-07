package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MOM", collectorNumber = "245")
public class KroxaAndKunoros extends Card {

    public KroxaAndKunoros() {
        ReturnCardFromGraveyardEffect returnCreature = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ExileNCardsFromGraveyardThenEffect(5, returnCreature),
                "Exile five cards from your graveyard?"));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new ExileNCardsFromGraveyardThenEffect(5, returnCreature),
                "Exile five cards from your graveyard?"));
    }
}
