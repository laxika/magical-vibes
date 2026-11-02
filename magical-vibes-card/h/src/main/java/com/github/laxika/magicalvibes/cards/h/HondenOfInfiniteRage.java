package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "172")
@CardRegistration(set = "EMA", collectorNumber = "134")
@CardRegistration(set = "HA3", collectorNumber = "17")
public class HondenOfInfiniteRage extends Card {

    public HondenOfInfiniteRage() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new DealDamageToAnyTargetEffect(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER)));
    }
}
