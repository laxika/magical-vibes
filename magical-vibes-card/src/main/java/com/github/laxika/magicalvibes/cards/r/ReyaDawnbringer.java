package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "35")
public class ReyaDawnbringer extends Card {

    public ReyaDawnbringer() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(new ReturnCardFromGraveyardEffect(GraveyardChoiceDestination.BATTLEFIELD, new CardTypePredicate(CardType.CREATURE)), "Return a creature from your graveyard to the battlefield?"));
    }
}
