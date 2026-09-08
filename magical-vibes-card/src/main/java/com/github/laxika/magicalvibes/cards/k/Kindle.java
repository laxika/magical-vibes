package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "TMP", collectorNumber = "184")
@CardRegistration(set = "TPR", collectorNumber = "137")
public class Kindle extends Card {

    public Kindle() {
        // 2 damage plus one for every card named Kindle sitting in any graveyard.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new Sum(
                new Fixed(2),
                new CardsInGraveyard(new CardNamedPredicate("Kindle"), CountScope.ANY_PLAYER))));
    }
}
