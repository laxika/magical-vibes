package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowPlayAndCastCardsExiledWithVoidCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseVoidCounterCardMayPlayEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1760")
public class DauthiVoidwalker extends Card {

    public DauthiVoidwalker() {
        addEffect(EffectSlot.STATIC,
                new ExileOpponentCardsInsteadOfGraveyardEffect(false, true));
        addEffect(EffectSlot.STATIC, new AllowPlayAndCastCardsExiledWithVoidCountersEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new ChooseVoidCounterCardMayPlayEffect()),
                "{T}, Sacrifice Dauthi Voidwalker: Choose an exiled card an opponent owns with a void counter on it. You may play it this turn without paying its mana cost."));
    }
}
