package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "117")
public class PhyrexianReaper extends Card {

    public PhyrexianReaper() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.GREEN)),
                        new DestroyTargetPermanentEffect(true)),
                TriggerMode.PER_BLOCKER);
    }
}
