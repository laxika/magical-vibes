package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.VoidCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EOE", collectorNumber = "91")
public class ChoraleOfTheVoid extends Card {

    public ChoraleOfTheVoid() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ATTACK, ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .source(GraveyardSearchScope.OPPONENT_GRAVEYARD)
                        .targetGraveyard(true)
                        .enterTapped(true)
                        .enterAttacking(true)
                        .build());
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                ConditionalEffect.unless(new NotCondition(new VoidCondition()), new SacrificeSelfEffect()));
    }
}
