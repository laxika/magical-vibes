package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "318")
@CardRegistration(set = "HML", collectorNumber = "92")
@CardRegistration(set = "ME1", collectorNumber = "125")
public class PrimalOrder extends Card {

    public PrimalOrder() {
        // At the beginning of each player's upkeep, this enchantment deals damage to that player
        // equal to the number of nonbasic lands they control.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new DealDamageToPlayersEffect(
                        new PermanentCount(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsLandPredicate(),
                                        new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))
                                )),
                                CountScope.TARGET_PLAYER),
                        DamageRecipient.ACTIVE_PLAYER));
    }
}
