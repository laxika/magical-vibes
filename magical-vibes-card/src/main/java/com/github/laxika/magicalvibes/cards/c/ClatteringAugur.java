package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "79")
public class ClatteringAugur extends Card {

    public ClatteringAugur() {
        // This creature can't block.
        addEffect(EffectSlot.STATIC, new CantBlockEffect());

        // When this creature enters, you draw a card and you lose 1 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(1));

        // {2}{B}{B}: Return this card from your graveyard to your hand.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{2}{B}{B}: Return this card from your graveyard to your hand."
        ));
    }
}
