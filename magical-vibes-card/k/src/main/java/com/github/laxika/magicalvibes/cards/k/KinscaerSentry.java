package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostControlledCountPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "22")
@CardRegistration(set = "ECL", collectorNumber = "300")
public class KinscaerSentry extends Card {

    public KinscaerSentry() {
        addEffect(EffectSlot.ON_ATTACK, new PutCardToBattlefieldEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardManaValueAtMostControlledCountPredicate(new PermanentIsAttackingPredicate()))),
                "creature", true, false, false, false, false, true));
    }
}
