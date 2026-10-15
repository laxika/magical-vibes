package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

public class GrubNotoriousAuntie extends Card {

    public GrubNotoriousAuntie() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new BlightEffect(1, new CreateTokenCopyOfTargetPermanentEffect(false, false, true, true)),
                "Blight 1 to create a tapped and attacking token copy of that creature?"));

        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayPayManaEffect("{B}", new TransformSelfEffect(),
                        "Pay {B} to transform Grub?"));
    }
}
