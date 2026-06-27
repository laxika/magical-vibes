package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GhastlyHaunting;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect;

@CardRegistration(set = "DKA", collectorNumber = "50")
public class SoulSeizer extends Card {

    public SoulSeizer() {
        GhastlyHaunting backFace = new GhastlyHaunting();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
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
