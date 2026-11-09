package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "47")
public class PendrellMists extends Card {

    public PendrellMists() {
        // All creatures have "At the beginning of your upkeep, sacrifice this creature unless you pay {1}."
        TriggeredAbility payOrLose = new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(new PayManaCost("{1}"), List.of(new SacrificeSelfEffect()), true));
        addStaticEffect(new GrantTriggeredAbilityEffect(payOrLose, GrantScope.ALL_CREATURES_INCLUDING_SELF));
    }
}
