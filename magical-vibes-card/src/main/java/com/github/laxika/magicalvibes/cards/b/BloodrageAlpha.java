package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YMID", collectorNumber = "36")
public class BloodrageAlpha extends Card {

    private static final CardPredicate WOLF_OR_WEREWOLF = new CardAnyOfPredicate(List.of(
            new CardSubtypePredicate(CardSubtype.WOLF),
            new CardSubtypePredicate(CardSubtype.WEREWOLF)));

    private static final PermanentPredicate ANOTHER_WOLF_OR_WEREWOLF_YOU_CONTROL =
            new PermanentAllOfPredicate(List.of(
                    new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WOLF, CardSubtype.WEREWOLF)),
                    new PermanentControlledBySourceControllerPredicate(),
                    new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

    private static final PermanentPredicate CREATURE_YOU_DONT_CONTROL = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

    public BloodrageAlpha() {
        TargetFilter wolf = new PermanentPredicateTargetFilter(
                ANOTHER_WOLF_OR_WEREWOLF_YOU_CONTROL,
                "Target must be another Wolf or Werewolf you control");
        var oneTimeBoon = RegisterDelayedControllerSpellCastTriggerEffect.oneShotUntilConsumed(
                WOLF_OR_WEREWOLF,
                List.of(new GrantTriggeredAbilityToCastSpellEffect(
                        EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(
                                new EnteringCreatureFightsTargetCreatureEffect(CREATURE_YOU_DONT_CONTROL),
                                "Have it fight up to one target creature you don't control?"))));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Another target Wolf or Werewolf you control fights target creature you don't control",
                        List.of(new FightTargetsEffect()),
                        List.of(wolf, TargetFilters.creatureAnOpponentControls())),
                new ChooseOneEffect.ChooseOneOption(
                        "You get a one-time boon with \"When you cast a Wolf or Werewolf spell, it gains 'When this creature enters, it fights up to one target creature you don't control.'\"",
                        oneTimeBoon)
        )));
    }
}
