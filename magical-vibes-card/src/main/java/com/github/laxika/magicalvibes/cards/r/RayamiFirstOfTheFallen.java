package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileNontokenCreaturesInsteadOfDyingWithBloodCounterEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsExiledWithSourceEffect;

import java.util.Set;

@CardRegistration(set = "C19", collectorNumber = "48")
public class RayamiFirstOfTheFallen extends Card {

    public RayamiFirstOfTheFallen() {
        addEffect(EffectSlot.STATIC, new ExileNontokenCreaturesInsteadOfDyingWithBloodCounterEffect());
        addEffect(EffectSlot.STATIC, new GainKeywordsOfCreatureCardsExiledWithSourceEffect(
                Set.of(
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
                        Keyword.VIGILANCE
                ), true, CounterType.BLOOD));
    }
}
