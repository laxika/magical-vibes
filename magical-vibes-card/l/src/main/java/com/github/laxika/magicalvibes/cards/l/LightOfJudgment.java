package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyUpToOneAttachedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIN", collectorNumber = "144")
public class LightOfJudgment extends Card {

    public LightOfJudgment() {
        // Light of Judgment deals 6 damage to target creature. Destroy up to one Equipment attached
        // to that creature.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(6))
                .addEffect(EffectSlot.SPELL, new DestroyUpToOneAttachedPermanentEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)));
    }
}
