package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsExiledWithSourceEffect;

import java.util.Set;

@CardRegistration(set = "FRF", collectorNumber = "84")
public class Soulflayer extends Card {

    public Soulflayer() {
        addEffect(EffectSlot.SPELL, new DelveCost());
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
                        Keyword.REACH,
                        Keyword.TRAMPLE,
                        Keyword.VIGILANCE
                ), false));
    }
}
