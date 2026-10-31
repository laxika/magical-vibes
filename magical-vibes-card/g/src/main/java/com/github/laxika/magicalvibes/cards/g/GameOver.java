package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AnOpponentLifeAtOrBelowHalfStartingLife;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "18")
public class GameOver extends Card {

    public GameOver() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AnyOf(List.of(
                        ControllerLifeAtMost.atHalfStartingLife(),
                        new AnOpponentLifeAtOrBelowHalfStartingLife())),
                new ReduceOwnCastCostEffect(new Fixed(2))));
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
