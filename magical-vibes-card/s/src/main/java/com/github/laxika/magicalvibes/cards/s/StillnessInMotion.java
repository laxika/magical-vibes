package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInLibraryAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromControllerGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "TDM", collectorNumber = "59")
public class StillnessInMotion extends Card {

    public StillnessInMotion() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new MillEffect(3, MillRecipient.CONTROLLER),
                ConditionalEffect.unless(
                        new NotCondition(new CardsInLibraryAtLeast(1)),
                        SequenceEffect.of(
                                new ExileSelfEffect(),
                                new PutCardsFromControllerGraveyardOnTopOfLibraryEffect(5)
                        ))));
    }
}
