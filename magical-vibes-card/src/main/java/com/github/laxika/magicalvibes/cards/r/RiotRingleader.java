package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "AVR", collectorNumber = "152")
public class RiotRingleader extends Card {

    public RiotRingleader() {
        // Whenever this creature attacks, Human creatures you control get +1/+0 until end
        // of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(1, 0,
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
    }
}
