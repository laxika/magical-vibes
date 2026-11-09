package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "307")
@CardRegistration(set = "ME3", collectorNumber = "212")
public class TheTabernacleAtPendrellVale extends Card {

    public TheTabernacleAtPendrellVale() {
        // All creatures have "At the beginning of your upkeep, destroy this creature unless you pay {1}."
        TriggeredAbility payOrLose = new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(new PayManaCost("{1}"), List.of(new DestroyReferencedPermanentEffect(PermanentReference.SOURCE)), true));
        addStaticEffect(new GrantTriggeredAbilityEffect(payOrLose, GrantScope.ALL_CREATURES_INCLUDING_SELF));
    }
}
