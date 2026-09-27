package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.OpponentCantAttackSourceControllerThisCombatEffect;

@CardRegistration(set = "LTC", collectorNumber = "10")
@CardRegistration(set = "LTC", collectorNumber = "94")
public class ChampionsOfMinasTirith extends Card {

    public ChampionsOfMinasTirith() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect());

        addEffect(EffectSlot.OPPONENT_BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(new ControllerIsMonarch(), MayPayManaEffect.dynamic(
                        new CardsInHand(CountScope.CONTROLLER),
                        null,
                        "Pay {X}, where X is the number of cards in your hand?",
                        MayPayPayer.ACTIVE_PLAYER,
                        new OpponentCantAttackSourceControllerThisCombatEffect())));
    }
}
