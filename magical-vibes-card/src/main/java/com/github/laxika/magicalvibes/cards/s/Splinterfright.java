package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInControllerGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ISD", collectorNumber = "205")
public class Splinterfright extends Card {

    public Splinterfright() {
        addEffect(EffectSlot.STATIC, new PowerToughnessEqualToCardsInControllerGraveyardEffect(
                new CardTypePredicate(CardType.CREATURE)
        ));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MillControllerEffect(2));
    }
}
