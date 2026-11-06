package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsOnlySinglePermanentOrPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "76")
@CardRegistration(set = "WHO", collectorNumber = "379")
@CardRegistration(set = "WHO", collectorNumber = "681")
@CardRegistration(set = "WHO", collectorNumber = "970")
public class BillPotts extends Card {

    public BillPotts() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)
        ));
        StackEntryAllOfPredicate targetsOnlyBill = new StackEntryAllOfPredicate(List.of(
                new StackEntryTargetsOnlySinglePermanentOrPlayerPredicate(),
                new StackEntryTargetsSourcePredicate()
        ));

        addTrigger(List.of(
                        Triggers.youCastSpell().withFilter(instantOrSorcery).withStackEntryFilter(targetsOnlyBill),
                        Triggers.youActivateAbility().withStackEntryFilter(targetsOnlyBill)),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CopyTriggeringSpellOrAbilityEffect());
    }
}
