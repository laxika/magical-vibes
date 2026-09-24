package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutRandomCardExiledWithSourceIntoOwnersHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "CMM", collectorNumber = "164")
public class GorexTheTombshell extends Card {

    public GorexTheTombshell() {
        addEffect(EffectSlot.SPELL,
                new ExileAnyNumberOfCardsFromGraveyardCost(new CardTypePredicate(CardType.CREATURE), 2));
        PutRandomCardExiledWithSourceIntoOwnersHandEffect returnExiledCard =
                new PutRandomCardExiledWithSourceIntoOwnersHandEffect();
        addEffect(EffectSlot.ON_ATTACK, returnExiledCard);
        addEffect(EffectSlot.ON_DEATH, returnExiledCard);
    }
}
