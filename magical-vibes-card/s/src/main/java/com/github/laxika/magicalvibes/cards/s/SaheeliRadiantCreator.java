package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DRC", collectorNumber = "3")
public class SaheeliRadiantCreator extends Card {

    public SaheeliRadiantCreator() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.ARTIFICER),
                        new CardTypePredicate(CardType.ARTIFACT))),
                List.of(new EnergyCountersEffect(1))));

        target(TargetFilters.permanentYouControl()).addEffect(
                EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayEnergyCost(3),
                        List.of(),
                        true,
                        List.of(new QueueReflexiveAbilityEffect(
                                new CreateTokenCopyOfTargetPermanentEffect(
                                        List.of(),
                                        Set.of(CardType.ARTIFACT, CardType.CREATURE),
                                        5,
                                        5,
                                        Map.of(),
                                        true,
                                        false,
                                        true,
                                        false)))));
    }
}
