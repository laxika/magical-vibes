package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsMatchingToGraveyardRestToBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1583")
public class TheFourteenthDoctor extends Card {

    public TheFourteenthDoctor() {
        addEffect(EffectSlot.ON_SELF_CAST,
                new RevealTopCardsMatchingToGraveyardRestToBottomRandomEffect(
                        14, new CardSubtypePredicate(CardSubtype.DOCTOR)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CopyCreatureCardInGraveyardOnEnterEffect(
                        null, null, null, Set.of(), new CardSubtypePredicate(CardSubtype.DOCTOR),
                        true, true, true, false));
    }
}
