package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsOnlySinglePermanentOrPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

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

        addTrigger(Triggers.youCastSpell(), new OncePerTurnTriggerEffect(
                CopyControllerCastSpellOnSpellCastEffect.withCastTargetCondition(
                        instantOrSorcery, targetsOnlyBill, Set.of())));
        addTrigger(Triggers.youActivateAbility(), new OncePerTurnTriggerEffect(
                new CopyControllerActivatedAbilityTriggerEffect(
                        null, null, false, false, targetsOnlyBill)));
    }
}
