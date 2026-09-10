package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "245")
public class OliviaCrimsonBride extends Card {

    public OliviaCrimsonBride() {
        addEffect(EffectSlot.ON_ATTACK,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .enterTapped(true)
                        .enterAttacking(true)
                        .battlefieldEffectGrants(List.of(
                                new GrantEffectEffect(
                                        new StateTriggerEffect(
                                                new PermanentNotPredicate(
                                                        new PermanentControllerControlsPermanentPredicate(
                                                                new PermanentAllOfPredicate(List.of(
                                                                        new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                                                                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))))),
                                                List.of(new ExileSelfEffect()),
                                                "When you don't control a legendary Vampire, exile this creature."),
                                        GrantScope.TARGET)))
                        .build());
    }
}
