package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsPaired;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;

@CardRegistration(set = "AVR", collectorNumber = "178")
public class FloweringLumberknot extends Card {

    public FloweringLumberknot() {
        // The Lumberknot has no soulbond of its own, so any pairing it has is necessarily with a
        // creature with soulbond — "paired" alone is the whole restriction.
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new SourceIsPaired(),
                "it's paired with a creature with soulbond"));
    }
}
