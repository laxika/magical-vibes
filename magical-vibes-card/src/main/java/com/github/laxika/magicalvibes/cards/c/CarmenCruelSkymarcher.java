package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostSourcePowerPredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "5")
@CardRegistration(set = "LCC", collectorNumber = "26")
public class CarmenCruelSkymarcher extends Card {

    public CarmenCruelSkymarcher() {
        // Whenever a player sacrifices a permanent, put a +1/+1 counter on Carmen and you gain 1 life.
        addEffect(EffectSlot.ON_ANY_PERMANENT_SACRIFICED, SequenceEffect.of(
                new PutCountersOnSourceEffect(1, 1, 1),
                new GainLifeEffect(1)
        ));

        // Whenever Carmen attacks, return up to one target permanent card with mana value less than
        // or equal to Carmen's power from your graveyard to the battlefield.
        addEffect(EffectSlot.ON_ATTACK, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardAllOfPredicate(List.of(
                        new CardIsPermanentPredicate(),
                        new CardManaValueAtMostSourcePowerPredicate())))
                .targetGraveyard(true)
                .upTo(true)
                .build());
    }
}
