package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;

import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "152")
@CardRegistration(set = "SPM", collectorNumber = "230")
@CardRegistration(set = "OM1", collectorNumber = "145")
public class SpiderWomanStunningSavior extends Card {

    public SpiderWomanStunningSavior() {
        addStaticEffect(new EnterPermanentsOfTypesTappedEffect(
                Set.of(CardType.ARTIFACT, CardType.CREATURE), true));
    }
}
