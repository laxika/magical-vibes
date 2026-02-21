package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "292")
public class RootMaze extends Card {

    public RootMaze() {
        addEffect(EffectSlot.STATIC, new EnterPermanentsOfTypesTappedEffect(Set.of(CardType.ARTIFACT, CardType.LAND)));
    }
}
