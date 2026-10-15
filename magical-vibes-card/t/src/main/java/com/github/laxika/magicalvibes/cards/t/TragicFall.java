package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH2", collectorNumber = "104")
public class TragicFall extends Card {

    public TragicFall() {
        // Target creature gets -3/-3 until end of turn.
        // Hellbent — That creature gets -13/-13 until end of turn instead if you have no cards in hand.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new ControllerHandEmpty(),
                new BoostTargetCreatureEffect(-3, -3),
                new BoostTargetCreatureEffect(-13, -13)
        ));
    }
}
