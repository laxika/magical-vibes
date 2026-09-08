package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "124")
public class CherishedHatchling extends Card {

    public CherishedHatchling() {
        addEffect(EffectSlot.ON_DEATH,
                new GrantFlashToCardTypeThisTurnEffect(new CardSubtypePredicate(CardSubtype.DINOSAUR)));
        addEffect(EffectSlot.ON_DEATH, new RegisterDelayedControllerSpellCastTriggerEffect(
                new CardSubtypePredicate(CardSubtype.DINOSAUR),
                List.of(new GrantTriggeredAbilityToCastSpellEffect(
                        EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(new EnteringCreatureFightsTargetCreatureEffect(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                                ))),
                                "Have it fight another target creature?"))),
                false));
    }
}
