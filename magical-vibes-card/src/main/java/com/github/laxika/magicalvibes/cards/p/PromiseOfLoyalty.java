package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCreaturePutsVowCounterThenSacrificesRestEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectsToCounterBearersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "161")
@CardRegistration(set = "MSC", collectorNumber = "142")
@CardRegistration(set = "MSC", collectorNumber = "319")
public class PromiseOfLoyalty extends Card {

    public PromiseOfLoyalty() {
        addEffect(EffectSlot.SPELL, new EachPlayerChoosesCreaturePutsVowCounterThenSacrificesRestEffect());
        addEffect(EffectSlot.SPELL, new GrantEffectsToCounterBearersEffect(CounterType.VOW, List.of(
                new CreaturesCantAttackControllerUnlessPredicateEffect(
                        new PermanentNotPredicate(new PermanentTruePredicate()), true))));
    }
}
