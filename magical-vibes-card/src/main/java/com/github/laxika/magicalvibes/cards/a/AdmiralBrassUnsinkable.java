package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "1")
@CardRegistration(set = "LCC", collectorNumber = "17")
@CardRegistration(set = "LCC", collectorNumber = "25")
@CardRegistration(set = "LCC", collectorNumber = "121")
public class AdmiralBrassUnsinkable extends Card {

    public AdmiralBrassUnsinkable() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(4, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(CardSubtype.PIRATE))))
                        .targetGraveyard(true)
                        .upTo(true)
                        .enterWithCounter(CounterType.FINALITY)
                        .enterWithCounterCount(1)
                        .grantHaste(true)
                        .battlefieldEffectGrants(List.of(
                                SetBasePowerToughnessEffect.indefinitely(4, 4)))
                        .build());
    }
}
