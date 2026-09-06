package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;

@CardRegistration(set = "FRF", collectorNumber = "157")
public class SilumgarTheDriftingDeath extends Card {

    public SilumgarTheDriftingDeath() {
        // Whenever a Dragon you control attacks, creatures defending player controls get -1/-1
        // until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DRAGON),
                        new BoostAllCreaturesEffect(-1, -1,
                                new PermanentControlledByDefendingPlayerPredicate())));
    }
}
