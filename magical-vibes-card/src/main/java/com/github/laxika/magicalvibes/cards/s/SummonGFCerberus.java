package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "FIN", collectorNumber = "162")
public class SummonGFCerberus extends Card {

    public SummonGFCerberus() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new SurveilEffect(1));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new CopyNextInstantOrSorceryCastThisTurnEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_III, new CopyNextInstantOrSorceryCastThisTurnEffect());
        addEffect(EffectSlot.SAGA_CHAPTER_III, new CopyNextInstantOrSorceryCastThisTurnEffect());
    }
}
