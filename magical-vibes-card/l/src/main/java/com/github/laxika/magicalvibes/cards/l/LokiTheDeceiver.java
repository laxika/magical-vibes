package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "86")
@CardRegistration(set = "MSC", collectorNumber = "407")
public class LokiTheDeceiver extends Card {

    public LokiTheDeceiver() {
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.VILLAIN),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                "Target must be another Villain you control"))
                .addTrigger(Triggers.attacks(), new CreateTokenCopyOfTargetPermanentEffect(
                        List.of(CardSubtype.ILLUSION), Set.of(), null, null, Map.of(),
                        false, false, true, true,
                        false, false, null, Set.of(), false, List.of(), List.of(), false, true,
                        new Fixed(1), false, Set.of(), true));

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.VILLAIN)),
                TriggerOptions.ONE_OR_MORE,
                new DrawCardEffect());
    }
}
