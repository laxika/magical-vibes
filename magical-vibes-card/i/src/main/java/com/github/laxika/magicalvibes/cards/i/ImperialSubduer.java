package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "19")
public class ImperialSubduer extends Card {

    public ImperialSubduer() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.creatureYouControlAttacks()
                                .withFilter(new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SAMURAI, CardSubtype.WARRIOR))),
                        TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                        new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
