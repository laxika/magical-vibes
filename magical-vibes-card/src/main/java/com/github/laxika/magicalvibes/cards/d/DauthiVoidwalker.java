package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardExiledWithVoidCounterMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentCardsInsteadOfGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MAR", collectorNumber = "63")
@CardRegistration(set = "AA3", collectorNumber = "8")
@CardRegistration(set = "MH2", collectorNumber = "81")
public class DauthiVoidwalker extends Card {

    public DauthiVoidwalker() {
        addEffect(EffectSlot.STATIC, ExileOpponentCardsInsteadOfGraveyardEffect.withVoidCounter());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new ChooseCardExiledWithVoidCounterMayPlayThisTurnEffect()),
                "{T}, Sacrifice Dauthi Voidwalker: Choose an exiled card an opponent owns with a void counter on it. You may play it this turn without paying its mana cost."
        ));
    }
}
