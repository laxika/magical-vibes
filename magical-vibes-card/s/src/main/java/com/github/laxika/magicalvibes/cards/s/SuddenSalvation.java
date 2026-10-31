package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachOpponentControllingReturnedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "VOC", collectorNumber = "10")
@CardRegistration(set = "VOC", collectorNumber = "48")
public class SuddenSalvation extends Card {

    public SuddenSalvation() {
        addSpellEffect(ReturnTargetCardsFromGraveyardToBattlefieldEffect.fromAllGraveyardsUnderOwnersControl(
                        new CardIsPermanentPredicate(), 3, true, true));
        addSpellEffect(new DrawCardForEachOpponentControllingReturnedPermanentEffect());
    }
}
