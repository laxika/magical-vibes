package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfChosenPermanentYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MAT", collectorNumber = "26")
@CardRegistration(set = "MAT", collectorNumber = "76")
@CardRegistration(set = "MAT", collectorNumber = "126")
@CardRegistration(set = "MAT", collectorNumber = "165")
@CardRegistration(set = "MAT", collectorNumber = "206")
public class CalixGuidedByFate extends Card {

    private static final PermanentPredicate NONLEGENDARY_ENCHANTMENT = new PermanentAllOfPredicate(List.of(
            new PermanentControlledBySourceControllerPredicate(),
            new PermanentIsEnchantmentPredicate(),
            new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))));

    private static final PermanentPredicate CALIX_OR_ENCHANTED_CREATURE = new PermanentAnyOfPredicate(List.of(
            new PermanentIsSourcePermanentPredicate(),
            new PermanentIsEnchantedPredicate()));

    public CalixGuidedByFate() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                OncePerTurnTriggerEffect.markOnAcceptance(new AllyCombatDamageTriggerEffect(
                        CALIX_OR_ENCHANTED_CREATURE,
                        new MayEffect(
                                new CreateTokenCopyOfChosenPermanentYouControlEffect(
                                        NONLEGENDARY_ENCHANTMENT, true),
                                "Create a token copy of a nonlegendary enchantment you control?"))));
    }
}
