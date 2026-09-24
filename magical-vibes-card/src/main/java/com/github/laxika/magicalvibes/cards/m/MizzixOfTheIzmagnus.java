package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueGreaterThanControllerExperienceCountersPredicate;

import java.util.List;

@CardRegistration(set = "2X2", collectorNumber = "257")
@CardRegistration(set = "C15", collectorNumber = "50")
public class MizzixOfTheIzmagnus extends Card {

    public MizzixOfTheIzmagnus() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addEffect(EffectSlot.STATIC,
                new ReduceCastCostForMatchingSpellsEffect(instantOrSorcery,
                        new ControllerExperienceCounters(), CostModificationScope.SELF));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(instantOrSorcery,
                        List.of(new ExperienceCountersEffect(1)),
                        new StackEntryManaValueGreaterThanControllerExperienceCountersPredicate()));
    }
}
