package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "52")
public class TreasureHunter extends Card {

    public TreasureHunter() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(new ReturnCardFromGraveyardEffect(GraveyardChoiceDestination.HAND, new CardTypePredicate(CardType.ARTIFACT)), "Return an artifact from your graveyard to your hand?"));
    }
}
