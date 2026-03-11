package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "146")
@CardRegistration(set = "M11", collectorNumber = "98")
public class Gravedigger extends Card {

    public Gravedigger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(new ReturnCardFromGraveyardEffect(GraveyardChoiceDestination.HAND, new CardTypePredicate(CardType.CREATURE)), "Return a creature card from your graveyard to your hand?"));
    }
}
