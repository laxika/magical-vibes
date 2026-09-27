package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesOfChosenSubtypeEffect;

import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "9")
public class AndTheyShallKnowNoFear extends Card {

    public AndTheyShallKnowNoFear() {
        addEffect(EffectSlot.SPELL,
                BoostAllCreaturesOfChosenSubtypeEffect.ownCreatures(
                        1, 0, Set.of(Keyword.INDESTRUCTIBLE)));
    }
}
