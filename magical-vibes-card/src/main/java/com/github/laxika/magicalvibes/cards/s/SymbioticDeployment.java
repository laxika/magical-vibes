package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "88")
public class SymbioticDeployment extends Card {

    public SymbioticDeployment() {
        addEffect(EffectSlot.STATIC, new SkipDrawStepEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new TapMultiplePermanentsCost(2, new PermanentIsCreaturePredicate()),
                        new DrawCardEffect(1)),
                "{1}, Tap two untapped creatures you control: Draw a card."
        ));
    }
}
