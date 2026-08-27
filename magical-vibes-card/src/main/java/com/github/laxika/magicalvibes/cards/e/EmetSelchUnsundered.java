package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HadesSorcererOfEld;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "FIN", collectorNumber = "218")
@CardRegistration(set = "FIN", collectorNumber = "394")
@CardRegistration(set = "FIN", collectorNumber = "483")
@CardRegistration(set = "FIN", collectorNumber = "539")
public class EmetSelchUnsundered extends Card {

    public EmetSelchUnsundered() {
        setBackFaceCard(new HadesSorcererOfEld());

        SequenceEffect loot = SequenceEffect.of(
                new DrawCardEffect(1),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, loot);
        addEffect(EffectSlot.ON_ATTACK, loot);

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new GraveyardCardThreshold(14, null),
                new MayEffect(new TransformSelfEffect(), "Transform Emet-Selch?")));
    }

    @Override
    public String getBackFaceClassName() {
        return "HadesSorcererOfEld";
    }
}
