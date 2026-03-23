package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerOtherAttackingSubtypeEffect;

@CardRegistration(set = "XLN", collectorNumber = "221")
public class DireFleetCaptain extends Card {

    public DireFleetCaptain() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfPerOtherAttackingSubtypeEffect(CardSubtype.PIRATE, 1, 1));
    }
}
