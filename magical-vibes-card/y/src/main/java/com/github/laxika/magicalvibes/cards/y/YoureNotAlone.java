package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIN", collectorNumber = "44")
public class YoureNotAlone extends Card {

    public YoureNotAlone() {
        // Target creature gets +2/+2 until end of turn. If you control three or more creatures,
        // it gets an additional +2/+2 until end of turn instead.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new ControlsPermanentCount(3, new PermanentIsCreaturePredicate()),
                        new BoostTargetCreatureEffect(2, 2)));
    }
}
