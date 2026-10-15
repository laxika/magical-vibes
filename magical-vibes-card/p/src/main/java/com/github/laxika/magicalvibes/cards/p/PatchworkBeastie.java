package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "DSK", collectorNumber = "195")
public class PatchworkBeastie extends Card {

    public PatchworkBeastie() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new Delirium(),
                "there are four or more card types among cards in your graveyard"
        ));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(new MillEffect(1, MillRecipient.CONTROLLER), "Mill a card?"));
    }
}
