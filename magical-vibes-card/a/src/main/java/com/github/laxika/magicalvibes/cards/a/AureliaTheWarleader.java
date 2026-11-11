package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "143")
@CardRegistration(set = "V15", collectorNumber = "4")
@CardRegistration(set = "GK1", collectorNumber = "77")
@CardRegistration(set = "2X2", collectorNumber = "179")
@CardRegistration(set = "MUL", collectorNumber = "35")
@CardRegistration(set = "MUL", collectorNumber = "100")
@CardRegistration(set = "MUL", collectorNumber = "165")
@CardRegistration(set = "MAR", collectorNumber = "86")
public class AureliaTheWarleader extends Card {

    public AureliaTheWarleader() {
        // Flying, vigilance and haste are auto-loaded keywords. "Whenever Aurelia attacks for the
        // first time each turn" is its first attack of the turn (TriggerLimit.FIRST_TIME_EACH_TURN), so
        // attacking again in the extra combat phase this grants doesn't re-trigger and loop.
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()),
                new AdditionalCombatPhaseEffect(1));
    }
}
