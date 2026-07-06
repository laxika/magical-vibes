package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "M10", collectorNumber = "114")
public class TendrilsOfCorruption extends Card {

    public TendrilsOfCorruption() {
        // Tendrils of Corruption deals X damage to target creature and you gain X life,
        // where X is the number of Swamps you control. The life gain doesn't depend on
        // the damage being dealt — both amounts count Swamps at resolution.
        PermanentCount swampCount = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(swampCount));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(swampCount));
    }
}
