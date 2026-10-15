package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.SelectiveAdaptationEffect;
import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "65")
public class SelectiveAdaptation extends Card {

    public SelectiveAdaptation() {
        addEffect(EffectSlot.SPELL, new SelectiveAdaptationEffect(7, List.of(
                Keyword.FLYING,
                Keyword.FIRST_STRIKE,
                Keyword.DOUBLE_STRIKE,
                Keyword.DEATHTOUCH,
                Keyword.HASTE,
                Keyword.HEXPROOF,
                Keyword.INDESTRUCTIBLE,
                Keyword.LIFELINK,
                Keyword.MENACE,
                Keyword.REACH,
                Keyword.TRAMPLE,
                Keyword.VIGILANCE)));
    }
}
