package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CreaturesDiedThisTurnAtLeast;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YECL", collectorNumber = "24")
public class PerennialGravewarden extends Card {

    public PerennialGravewarden() {
        // When this creature enters, it perpetually gets +1/+1.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PerpetuallyBoostSourceEffect(1, 1));

        // At the beginning of your end step, if a creature entered and a creature died this turn,
        // return this card from your graveyard to the battlefield tapped.
        addEffect(EffectSlot.GRAVEYARD_CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new AllOf(List.of(
                                new PermanentEnteredThisTurn(
                                        new CardTypePredicate(CardType.CREATURE), 1, CountScope.ANY_PLAYER),
                                new CreaturesDiedThisTurnAtLeast(1))),
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(true)));
    }
}
