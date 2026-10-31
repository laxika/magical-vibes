package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastNotFromHand;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;

@CardRegistration(set = "M21", collectorNumber = "69")
@CardRegistration(set = "M21", collectorNumber = "352")
public class SeeTheTruth extends Card {

    public SeeTheTruth() {
        addSpellEffect(new ConditionalEffect(new NotCondition(new CastNotFromHand()),
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3))));
        addSpellEffect(new ConditionalEffect(new CastNotFromHand(),
                new LookAtTopCardsEffect(new Fixed(3), new Fixed(3), null,
                        LookDestination.BOTTOM_OF_LIBRARY, false)));
    }
}
