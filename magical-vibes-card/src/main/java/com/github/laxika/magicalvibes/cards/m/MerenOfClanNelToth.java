package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetGraveyardCardManaValueAtMostControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "CMM", collectorNumber = "346")
@CardRegistration(set = "CMM", collectorNumber = "584")
@CardRegistration(set = "CMM", collectorNumber = "685")
@CardRegistration(set = "C15", collectorNumber = "49")
public class MerenOfClanNelToth extends Card {

    public MerenOfClanNelToth() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new ExperienceCountersEffect(1));

        TargetGraveyardCardManaValueAtMostControllerExperienceCounters withinExperience =
                new TargetGraveyardCardManaValueAtMostControllerExperienceCounters();
        ReturnCardFromGraveyardEffect toBattlefield = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();
        ReturnCardFromGraveyardEffect toHand = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();

        target(new GraveyardCardPredicateTargetFilter(
                new CardTypePredicate(CardType.CREATURE), GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                        ConditionalEffect.unless(withinExperience, toBattlefield),
                        ConditionalEffect.unless(new NotCondition(withinExperience), toHand)));
    }
}
