package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BouncePermanentOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "312")
@CardRegistration(set = "DMR", collectorNumber = "237")
public class Umbilicus extends Card {

    public Umbilicus() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                ForcedCostOrElseEffect.enchantedControllerMayPay(
                        new PayLifeCost(2),
                        List.of(new BouncePermanentOnUpkeepEffect(
                                BouncePermanentOnUpkeepEffect.Scope.TRIGGER_TARGET_PLAYER,
                                Set.of(),
                                "Choose a permanent to return to its owner's hand."))));
    }
}
