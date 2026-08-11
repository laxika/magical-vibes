package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ODY", collectorNumber = "208")
public class Mudhole extends Card {

    public Mudhole() {
        addEffect(EffectSlot.SPELL, new ExileGraveyardCardsEffect(
                0, GraveyardExileScope.TARGET_PLAYER_ALL_MATCHING, new CardTypePredicate(CardType.LAND)));
    }
}
