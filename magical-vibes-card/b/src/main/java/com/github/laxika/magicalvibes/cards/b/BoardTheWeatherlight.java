package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

@CardRegistration(set = "DOM", collectorNumber = "8")
public class BoardTheWeatherlight extends Card {

    public BoardTheWeatherlight() {
        addSpellEffect(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(5, new CardIsHistoricPredicate()));
    }
}
