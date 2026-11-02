package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsMatchingToGraveyardRestToBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1583")
public class TheFourteenthDoctor extends Card {

    public TheFourteenthDoctor() {
        addTrigger(Triggers.youCastThisSpell(),
                new RevealTopCardsMatchingToGraveyardRestToBottomRandomEffect(
                        14, new CardSubtypePredicate(CardSubtype.DOCTOR)));
        addStaticEffect(new CopyCreatureCardInGraveyardOnEnterEffect(
                        null, null, null, Set.of(), new CardSubtypePredicate(CardSubtype.DOCTOR),
                        true, true, true, false));
    }
}
