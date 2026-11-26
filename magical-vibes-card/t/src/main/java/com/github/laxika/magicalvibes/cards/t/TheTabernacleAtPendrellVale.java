package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "LEG", collectorNumber = "307")
@CardRegistration(set = "ME3", collectorNumber = "212")
public class TheTabernacleAtPendrellVale extends Card {

    public TheTabernacleAtPendrellVale() {
        // All creatures have "At the beginning of your upkeep, destroy this creature unless you pay {1}."
        TriggeredAbility payOrLose = new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{1}", new DestroyTargetPermanentEffect(Subjects.source())));
        addStaticEffect(new GrantTriggeredAbilityEffect(payOrLose, Subjects.allCreatures()));
    }
}
