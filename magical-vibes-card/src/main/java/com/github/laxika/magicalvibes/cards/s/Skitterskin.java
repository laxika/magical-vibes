package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "97")
public class Skitterskin extends Card {

    public Skitterskin() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate this creature. Activate only if you control another colorless creature."
        ).withActivationCondition(
                new ControlsAnotherPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsColorlessPredicate(),
                        new PermanentIsCreaturePredicate()
                ))),
                "Activate only if you control another colorless creature"
        ));
    }
}
