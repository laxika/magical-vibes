package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueGreaterThanControllerExperienceCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "2X2", collectorNumber = "257")
@CardRegistration(set = "C15", collectorNumber = "50")
@CardRegistration(set = "CMM", collectorNumber = "348")
@CardRegistration(set = "CMM", collectorNumber = "586")
public class MizzixOfTheIzmagnus extends Card {

    public MizzixOfTheIzmagnus() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(instantOrSorcery,
                        new ControllerExperienceCounters(), CostModificationScope.SELF));
        addTrigger(Triggers.youCastSpell()
                        .withFilter(instantOrSorcery)
                        .withStackEntryFilter(new StackEntryManaValueGreaterThanControllerExperienceCountersPredicate()),
                new ExperienceCountersEffect(1));
    }
}
