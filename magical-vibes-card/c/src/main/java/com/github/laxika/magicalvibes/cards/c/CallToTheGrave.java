package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "85")
@CardRegistration(set = "SCG", collectorNumber = "58")
public class CallToTheGrave extends Card {

    public CallToTheGrave() {
        // At the beginning of each player's upkeep, that player sacrifices a non-Zombie creature of their choice.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new SacrificePermanentsEffect(1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)))),
                SacrificeRecipient.ACTIVE_PLAYER));

        // At the beginning of the end step, if no creatures are on the battlefield, sacrifice this enchantment.
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AnyPlayerControlsPermanentCountAtMost(0, new PermanentIsCreaturePredicate())),
                new SacrificeSelfEffect());
    }
}
