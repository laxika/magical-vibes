package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ScryIfPlayerDealtDamageThisWayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MID", collectorNumber = "154")
@CardRegistration(set = "DBL", collectorNumber = "154")
public class PlayWithFire extends Card {

    public PlayWithFire() {
        addSpellEffect(SequenceEffect.of(
                        new DealDamageToAnyTargetEffect(2),
                        new ScryIfPlayerDealtDamageThisWayEffect()));
    }
}
