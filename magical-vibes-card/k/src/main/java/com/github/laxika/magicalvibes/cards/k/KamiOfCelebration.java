package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEC", collectorNumber = "20")
@CardRegistration(set = "NEC", collectorNumber = "59")
public class KamiOfCelebration extends Card {

    public KamiOfCelebration() {
        addTrigger(Triggers.creatureYouControlAttacks(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsModifiedPredicate(),
                        new ExileTopCardMayPlayThisTurnEffect(false)));

        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.EXILE)),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
