package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "76")
public class BillPotts extends Card {

    public BillPotts() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)
        ));
        StackEntryAllOfPredicate targetsOnlyBill = new StackEntryAllOfPredicate(List.of(
                new StackEntryIsSingleTargetPredicate(),
                new StackEntryTargetsSourcePredicate()
        ));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new OncePerTurnTriggerEffect(
                CopyControllerCastSpellOnSpellCastEffect.withCastTargetCondition(
                        instantOrSorcery, targetsOnlyBill, Set.of())));
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_ABILITY, new OncePerTurnTriggerEffect(
                new CopyControllerActivatedAbilityTriggerEffect(
                        null, null, false, false, targetsOnlyBill)));
    }
}
