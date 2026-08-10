package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MRD", collectorNumber = "203")
public class MaskOfMemory extends Card {

    public MaskOfMemory() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayEffect(
                SequenceEffect.of(
                        new DrawCardEffect(2),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                ),
                "Draw two cards and discard a card?"
        ));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
