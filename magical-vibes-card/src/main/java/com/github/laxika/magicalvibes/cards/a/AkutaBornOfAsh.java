package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.ControllerHasMoreCardsInHandThanEachOpponent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SOK", collectorNumber = "61")
public class AkutaBornOfAsh extends Card {

    public AkutaBornOfAsh() {
        addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new ControllerHasMoreCardsInHandThanEachOpponent(),
                        new MayEffect(
                                new SacrificePermanentThenEffect(
                                        new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                                        ReturnCardFromGraveyardEffect.builder()
                                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                                .filter(new CardIsSelfPredicate())
                                                .returnAll(true)
                                                .build(),
                                        "a Swamp"),
                                "Sacrifice a Swamp to return Akuta, Born of Ash from your graveyard to the battlefield?")));
    }
}
