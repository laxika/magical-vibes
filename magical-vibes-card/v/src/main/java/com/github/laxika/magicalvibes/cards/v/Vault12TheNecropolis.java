package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TotalRadCountersAmongPlayers;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GiveEachPlayerRadCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PIP", collectorNumber = "51")
@CardRegistration(set = "PIP", collectorNumber = "579")
public class Vault12TheNecropolis extends Card {

    public Vault12TheNecropolis() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new GiveEachPlayerRadCounterEffect(3));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new CreateTokenEffect(
                new TotalRadCountersAmongPlayers(), "Zombie Mutant", 2, 2, CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE, CardSubtype.MUTANT), Set.of(), Set.of()));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 2,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ZOMBIE, CardSubtype.MUTANT))))));
    }
}
