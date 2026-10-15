package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesOfChosenSubtypeEffect;

import java.util.Set;

@CardRegistration(set = "LGN", collectorNumber = "142")
public class TribalForcemage extends Card {

    public TribalForcemage() {
        addMorph("{1}{G}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new BoostAllCreaturesOfChosenSubtypeEffect(2, 2, Set.of(Keyword.TRAMPLE)));
    }
}
