package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "38")
public class IncursionSpecialist extends Card {

    public IncursionSpecialist() {
        // Whenever you cast your second spell each turn, this creature gets +2/+0 until end of
        // turn and can't be blocked this turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new NthSpellCastTriggerEffect(
                2,
                List.of(new BoostSelfEffect(2, 0), new MakeCreatureUnblockableEffect(true))
        ));
    }
}
