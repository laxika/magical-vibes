package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MMQ", collectorNumber = "21")
public class HonorTheFallen extends Card {

    public HonorTheFallen() {
        addEffect(EffectSlot.SPELL, new ExileGraveyardCardsEffect(
                0, GraveyardExileScope.ALL_PLAYERS, new CardTypePredicate(CardType.CREATURE)));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new Scaled(new EventValue(), 1)));
    }
}
