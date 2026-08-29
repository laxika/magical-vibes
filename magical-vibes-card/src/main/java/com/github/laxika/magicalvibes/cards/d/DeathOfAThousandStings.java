package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.ControllerHasMoreCardsInHandThanEachOpponent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

@CardRegistration(set = "SOK", collectorNumber = "64")
public class DeathOfAThousandStings extends Card {

    public DeathOfAThousandStings() {
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
        addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new ControllerHasMoreCardsInHandThanEachOpponent(),
                        new MayEffect(
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.HAND)
                                        .filter(new CardIsSelfPredicate())
                                        .returnAll(true)
                                        .build(),
                                "Return Death of a Thousand Stings from your graveyard to your hand?")));
    }
}
