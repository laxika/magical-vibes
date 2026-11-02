package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GhastlyHaunting;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "50")
public class SoulSeizer extends Card {

    public SoulSeizer() {
        setBackFaceCard(new GhastlyHaunting());

        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(
                        new TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect(),
                        "Transform Soul Seizer into Ghastly Haunting and attach it to a creature that player controls?"
                ));
    }

    @Override
    public String getBackFaceClassName() {
        return "GhastlyHaunting";
    }
}
