package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "148")
public class AftermathAnalyst extends Card {

    public AftermathAnalyst() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(3, MillRecipient.CONTROLLER));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new ReturnCardsFromControllerGraveyardToBattlefieldEffect(
                                new CardTypePredicate(CardType.LAND), Integer.MAX_VALUE, false, null, true)
                ),
                "{3}{G}, Sacrifice this creature: Return all land cards from your graveyard to the battlefield tapped."
        ));
    }
}
