package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "98")
@CardRegistration(set = "MSC", collectorNumber = "419")
public class TChakaVenerableKing extends Card {

    public TChakaVenerableKing() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MillControllerAndMayReturnMilledPermanentToHandEffect(
                        3,
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.LAND)))));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new ExileSelfFromGraveyardCost(), new BecomeMonarchEffect()),
                "{3}, Exile this card from your graveyard: You become the monarch. Activate only if you control your commander."
        ).withActivationCondition(
                new ControlsPermanent(new PermanentIsCommanderPredicate()),
                "Activate only if you control your commander."
        ));
    }
}
