package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;

import java.util.Set;

@CardRegistration(set = "FCA", collectorNumber = "53")
public class IshaiOjutaiDragonspeaker extends Card {

    public IshaiOjutaiDragonspeaker() {
        // Whenever an opponent casts a spell, put a +1/+1 counter on Ishai.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(Set.of(), 1, false));
    }
}
