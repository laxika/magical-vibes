package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

@CardRegistration(set = "DOM", collectorNumber = "8")
public class BoardTheWeatherlight extends Card {

    public BoardTheWeatherlight() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(5, new CardIsHistoricPredicate()));
    }
}
