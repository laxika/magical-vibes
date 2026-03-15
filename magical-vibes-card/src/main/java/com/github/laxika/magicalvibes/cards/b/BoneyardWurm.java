package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInControllerGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ISD", collectorNumber = "171")
public class BoneyardWurm extends Card {

    public BoneyardWurm() {
        addEffect(EffectSlot.STATIC, new PowerToughnessEqualToCardsInControllerGraveyardEffect(
                new CardTypePredicate(CardType.CREATURE)
        ));
    }
}
