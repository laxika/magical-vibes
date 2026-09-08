package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

@CardRegistration(set = "RTR", collectorNumber = "18")
@CardRegistration(set = "AKR", collectorNumber = "33")
@CardRegistration(set = "BIG", collectorNumber = "4")
public class RestInPeace extends Card {

    public RestInPeace() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS));
        // "If a card or token would be put into a graveyard from anywhere, exile it instead" —
        // the controller-side and opponent-side halves of the replacement together cover every player.
        addEffect(EffectSlot.STATIC, new ExileOwnCardsInsteadOfGraveyardEffect());
        addEffect(EffectSlot.STATIC, new ExileOpponentCardsInsteadOfGraveyardEffect());
    }
}
