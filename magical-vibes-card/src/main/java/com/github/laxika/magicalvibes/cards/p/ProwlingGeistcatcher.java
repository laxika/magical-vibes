package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSacrificedPermanentWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "MIC", collectorNumber = "21")
@CardRegistration(set = "MIC", collectorNumber = "59")
public class ProwlingGeistcatcher extends Card {

    public ProwlingGeistcatcher() {
        PermanentPredicate anotherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));

        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        anotherCreature,
                        new ExileSacrificedPermanentWithSourceEffect()));

        PermanentPredicate anotherCreatureToken = new PermanentAllOfPredicate(List.of(
                anotherCreature,
                new PermanentIsTokenPredicate()));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        anotherCreatureToken,
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ReturnAllCardsExiledWithSourceEffect(true));
    }
}
