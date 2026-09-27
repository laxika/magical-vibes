package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsChosenPermanentPredicate;

@CardRegistration(set = "YMID", collectorNumber = "30")
public class PredatorySludge extends Card {

    public PredatorySludge() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOpponentPermanentOnEnterEffect());
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(new PermanentIsChosenPermanentPredicate(),
                        new ConjureCardNamedIntoHandEffect("Predatory Sludge", false)));
    }
}
