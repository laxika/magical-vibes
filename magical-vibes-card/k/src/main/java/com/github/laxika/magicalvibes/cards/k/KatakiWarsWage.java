package com.github.laxika.magicalvibes.cards.k;

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

@CardRegistration(set = "SOK", collectorNumber = "14")
@CardRegistration(set = "MD1", collectorNumber = "24")
@CardRegistration(set = "MMA", collectorNumber = "20")
@CardRegistration(set = "MB2", collectorNumber = "148")
public class KatakiWarsWage extends Card {

    public KatakiWarsWage() {
        // All artifacts have "At the beginning of your upkeep, sacrifice this artifact unless you pay {1}."
        TriggeredAbility payOrLose = new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(new PayManaCost("{1}"), List.of(new SacrificeSelfEffect()), true));
        addStaticEffect(new GrantTriggeredAbilityEffect(payOrLose, GrantScope.ALL_PERMANENTS,
                new PermanentIsArtifactPredicate()));
        addStaticEffect(new GrantTriggeredAbilityEffect(payOrLose, GrantScope.SELF,
                new PermanentIsArtifactPredicate()));
    }
}
