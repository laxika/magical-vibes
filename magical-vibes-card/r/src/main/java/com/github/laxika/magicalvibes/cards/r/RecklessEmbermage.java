package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "189")
@CardRegistration(set = "7ED", collectorNumber = "212")
@CardRegistration(set = "6ED", collectorNumber = "201")
public class RecklessEmbermage extends Card {

    public RecklessEmbermage() {
        // {1}{R}: This creature deals 1 damage to any target and 1 damage to itself.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(SequenceEffect.simultaneous(
                        DealDamageEffect.toAnyTarget(1),
                        DealDamageEffect.to(Subjects.source(), 1)
                )),
                "{1}{R}: Reckless Embermage deals 1 damage to any target and 1 damage to itself."
        ));
    }
}
