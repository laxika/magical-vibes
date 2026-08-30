package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;

import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "152")
@CardRegistration(set = "SPM", collectorNumber = "230")
public class SpiderWomanStunningSavior extends Card {

    public SpiderWomanStunningSavior() {
        addEffect(EffectSlot.STATIC, new EnterPermanentsOfTypesTappedEffect(
                Set.of(CardType.ARTIFACT, CardType.CREATURE), true));
    }
}
