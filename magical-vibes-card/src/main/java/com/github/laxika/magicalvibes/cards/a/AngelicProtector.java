package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "TMP", collectorNumber = "2")
@CardRegistration(set = "TPR", collectorNumber = "2")
public class AngelicProtector extends Card {

    public AngelicProtector() {
        // Whenever this creature becomes the target of a spell or ability,
        // this creature gets +0/+3 until end of turn.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY, new BoostSelfEffect(0, 3));
    }
}
