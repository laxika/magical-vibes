package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfControlsPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DOM", collectorNumber = "152")
public class WizardsLightning extends Card {

    public WizardsLightning() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfControlsPermanentEffect(
                new PermanentHasSubtypePredicate(CardSubtype.WIZARD), 2));
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
    }
}
