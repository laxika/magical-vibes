package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardToughnessAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "44")
public class HookHorror extends Card {

    public HookHorror() {
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new PerpetuallyBoostSourceEffect(-1, -1),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardIsSelfPredicate(),
                                new CardToughnessAtLeastPredicate(1))))
                        .returnAll(true)
                        .underOwnersControl(true)
                        .build()));
    }
}
