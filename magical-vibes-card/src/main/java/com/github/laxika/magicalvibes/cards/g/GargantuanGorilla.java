package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GargantuanGorillaUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "91")
public class GargantuanGorilla extends Card {

    public GargantuanGorilla() {
        // At the beginning of your upkeep, you may sacrifice a Forest. If you sacrifice a snow Forest
        // this way, this creature gains trample until end of turn. If you don't sacrifice a Forest,
        // sacrifice this creature and it deals 7 damage to you.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new GargantuanGorillaUpkeepEffect());

        // {T}: This creature deals damage equal to its power to another target creature. That
        // creature deals damage equal to its power to this creature. (fight mechanic)
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new SourceFightsTargetCreatureEffect()),
                "{T}: This creature deals damage equal to its power to another target creature. "
                        + "That creature deals damage equal to its power to this creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                        "Target must be another creature")
        ));
    }
}
