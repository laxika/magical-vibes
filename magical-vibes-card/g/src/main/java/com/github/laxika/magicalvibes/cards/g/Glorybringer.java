package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ExertAsItAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "134")
@CardRegistration(set = "AKR", collectorNumber = "157")
@CardRegistration(set = "TDC", collectorNumber = "215")
public class Glorybringer extends Card {

    public Glorybringer() {
        // Flying and haste are Scryfall-loaded keywords — no wiring needed.

        addStaticEffect(new ExertAsItAttacksEffect());
        addTrigger(Triggers.exertedAsItAttacks(), new DealDamageToTargetCreatureEffect(4,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DRAGON))
                                ))));
    }
}
