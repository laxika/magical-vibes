package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "83")
@CardRegistration(set = "ME4", collectorNumber = "48")
@CardRegistration(set = "4ED", collectorNumber = "68")
@CardRegistration(set = "ITP", collectorNumber = "8")
@CardRegistration(set = "RQS", collectorNumber = "8")
@CardRegistration(set = "MMQ", collectorNumber = "78")
@CardRegistration(set = "SUM", collectorNumber = "57")
@CardRegistration(set = "ATQ", collectorNumber = "9")
@CardRegistration(set = "3ED", collectorNumber = "57")
@CardRegistration(set = "MB2", collectorNumber = "161")
public class EnergyFlux extends Card {

    public EnergyFlux() {
        // All artifacts have "At the beginning of your upkeep, sacrifice this artifact unless you pay {2}."
        TriggeredAbility payOrLose = new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(new PayManaCost("{2}"), List.of(new SacrificeSelfEffect()), true));
        addStaticEffect(new GrantTriggeredAbilityEffect(payOrLose, GrantScope.ALL_PERMANENTS,
                new PermanentIsArtifactPredicate()));
        addStaticEffect(new GrantTriggeredAbilityEffect(payOrLose, GrantScope.SELF,
                new PermanentIsArtifactPredicate()));
    }
}
