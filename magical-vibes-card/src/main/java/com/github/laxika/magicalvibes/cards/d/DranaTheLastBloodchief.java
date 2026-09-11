package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerChoosesCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "98")
public class DranaTheLastBloodchief extends Card {

    public DranaTheLastBloodchief() {
        addEffect(EffectSlot.ON_ATTACK, new DefendingPlayerChoosesCardFromGraveyardToBattlefieldEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardNotPredicate(new CardSupertypePredicate(CardSupertype.LEGENDARY))
                ))));
    }
}
