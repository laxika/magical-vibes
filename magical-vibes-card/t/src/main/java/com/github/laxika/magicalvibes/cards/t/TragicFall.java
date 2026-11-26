package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH2", collectorNumber = "104")
public class TragicFall extends Card {

    public TragicFall() {
        // Target creature gets -3/-3 until end of turn.
        // Hellbent — That creature gets -13/-13 until end of turn instead if you have no cards in hand.
        target(TargetFilters.creature()).addSpellEffect(new ConditionalReplacementEffect(
                new ControllerHandEmpty(),
                BoostEffect.target(-3, -3),
                BoostEffect.target(-13, -13)
        ));
    }
}
