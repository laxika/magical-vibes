package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CloakTopCardOfControllerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentsEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "18")
@CardRegistration(set = "MKC", collectorNumber = "329")
public class VeiledAscension extends Card {

    public VeiledAscension() {
        PermanentPredicate faceDownCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsFaceDownPredicate()));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCounterOnEachControlledPermanentEffect(
                CounterType.FLYING, 1, faceDownCreature));
        addEffect(EffectSlot.STATIC, new ControlledPermanentsEnterWithAdditionalCountersEffect(
                faceDownCreature, CounterType.FLYING, 1));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new CloakTopCardOfControllerLibraryEffect(),
                "Cloak the top card of your library?"));
    }
}
