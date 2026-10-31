package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SPM", collectorNumber = "51")
@CardRegistration(set = "SPM", collectorNumber = "221")
@CardRegistration(set = "OM1", collectorNumber = "57")
public class BeholdTheSinisterSix extends Card {

    public BeholdTheSinisterSix() {
        setMultiTargetConstraint(MultiTargetConstraint.DIFFERENT_NAMES);
        addSpellEffect(new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                new CardTypePredicate(CardType.CREATURE), 6, false, false));
    }
}
