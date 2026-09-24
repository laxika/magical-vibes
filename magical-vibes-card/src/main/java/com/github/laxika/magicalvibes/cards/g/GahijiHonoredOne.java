package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOpponentOrTheirPlaneswalkerPredicate;

@CardRegistration(set = "C13", collectorNumber = "191")
public class GahijiHonoredOne extends Card {

    public GahijiHonoredOne() {
        // Whenever a creature attacks one of your opponents or a planeswalker an opponent controls,
        // that creature gets +2/+0 until end of turn.
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, new TriggeringPermanentConditionalEffect(
                new PermanentIsAttackingOpponentOrTheirPlaneswalkerPredicate(),
                new BoostTargetCreatureEffect(2, 0)));
    }
}
