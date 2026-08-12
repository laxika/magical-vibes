package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandAtEndStepEffect;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "105")
public class Rakalite extends Card {

    public Rakalite() {
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(
                        PreventDamageEffect.nextToTarget(1),
                        new ReturnSelfToHandAtEndStepEffect()
                ),
                "{2}: Prevent the next 1 damage that would be dealt to any target this turn. Return this artifact to its owner's hand at the beginning of the next end step."));
    }
}
