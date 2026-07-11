package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

/**
 * Natural Affinity — {2}{G} Instant
 *
 * All lands become 2/2 creatures until end of turn. They're still lands.
 */
@CardRegistration(set = "9ED", collectorNumber = "256")
public class NaturalAffinity extends Card {

    public NaturalAffinity() {
        addEffect(EffectSlot.SPELL, new AnimatePermanentsEffect(
                2, 2,
                List.of(), Set.of(),
                null, Set.of(),
                GrantScope.ALL_LANDS, EffectDuration.UNTIL_END_OF_TURN
        ));
    }
}
