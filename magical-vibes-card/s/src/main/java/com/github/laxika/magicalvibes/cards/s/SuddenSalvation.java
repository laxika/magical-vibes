package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachOpponentControllingReturnedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "VOC", collectorNumber = "10")
@CardRegistration(set = "VOC", collectorNumber = "48")
public class SuddenSalvation extends Card {

    public SuddenSalvation() {
        addEffect(EffectSlot.SPELL,
                ReturnTargetCardsFromGraveyardToBattlefieldEffect.fromAllGraveyardsUnderOwnersControl(
                        new CardIsPermanentPredicate(), 3, true, true));
        addEffect(EffectSlot.SPELL, new DrawCardForEachOpponentControllingReturnedPermanentEffect());
    }
}
