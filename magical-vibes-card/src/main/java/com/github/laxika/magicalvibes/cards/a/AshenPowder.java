package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "6ED", collectorNumber = "112")
public class AshenPowder extends Card {

    public AshenPowder() {
        // Put target creature card from an opponent's graveyard onto the battlefield under your control.
        addEffect(EffectSlot.SPELL, new PutCardFromOpponentGraveyardOntoBattlefieldEffect(
                false, new CardTypePredicate(CardType.CREATURE), false));
    }
}
