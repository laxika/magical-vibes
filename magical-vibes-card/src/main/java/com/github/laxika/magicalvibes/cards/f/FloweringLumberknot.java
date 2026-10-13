package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourcePairedWithSoulboundCreature;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;

@CardRegistration(set = "AVR", collectorNumber = "178")
public class FloweringLumberknot extends Card {

    public FloweringLumberknot() {
        // The partner may lose soulbond while the pairing stays, so the partner's keyword is checked too.
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new SourcePairedWithSoulboundCreature(),
                "it's paired with a creature with soulbond"));
    }
}
