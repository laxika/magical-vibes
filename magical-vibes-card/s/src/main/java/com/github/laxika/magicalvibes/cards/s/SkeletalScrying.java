package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "ODY", collectorNumber = "161")
@CardRegistration(set = "C14", collectorNumber = "162")
@CardRegistration(set = "C17", collectorNumber = "125")
public class SkeletalScrying extends Card {

    public SkeletalScrying() {
        addCastingCost(new ExileXCardsFromGraveyardCost());
        addSpellEffect(new DrawCardEffect(new XValue()));
        addSpellEffect(new LoseLifeEffect(new XValue(), LoseLifeRecipient.CONTROLLER));
    }
}
