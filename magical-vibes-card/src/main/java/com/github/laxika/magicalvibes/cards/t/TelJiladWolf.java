package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenCombatOpponentMatchesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "DST", collectorNumber = "88")
public class TelJiladWolf extends Card {

    public TelJiladWolf() {
        // Whenever this creature becomes blocked by an artifact creature, this creature gets +3/+3
        // until end of turn.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new BoostSelfWhenCombatOpponentMatchesEffect(new PermanentIsArtifactPredicate(), 3, 3));
    }
}
