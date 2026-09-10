package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTargetFaceDownPermanentAndMayTurnFaceUpEffect;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "182")
public class HauntwoodsShrieker extends Card {

    public HauntwoodsShrieker() {
        addEffect(EffectSlot.ON_ATTACK, ManifestDreadEffect.forController());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new RevealTargetFaceDownPermanentAndMayTurnFaceUpEffect()),
                "{1}{G}: Reveal target face-down permanent. If it's a creature card, you may turn it face up."
        ));
    }
}
