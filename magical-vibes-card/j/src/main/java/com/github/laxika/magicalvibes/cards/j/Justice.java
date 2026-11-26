package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "41")
@CardRegistration(set = "ICE", collectorNumber = "32")
public class Justice extends Card {

    public Justice() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {W}{W}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{W}{W}", new SacrificeSelfEffect()));

        // Whenever a red creature or spell deals damage, this enchantment deals that much
        // damage to that creature's or spell's controller.
        addTrigger(Triggers.sourceDealsDamage()
                        .withFilter(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.RED)))))
                        .withSpellFilter(new CardColorPredicate(CardColor.RED)),
                DealDamageEffect.to(Subjects.controllerOf(Subjects.triggering()), new EventValue()));
    }
}
