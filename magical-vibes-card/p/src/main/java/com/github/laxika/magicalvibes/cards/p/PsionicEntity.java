package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "95")
@CardRegistration(set = "LEG", collectorNumber = "67")
public class PsionicEntity extends Card {

    public PsionicEntity() {
        // {T}: This creature deals 2 damage to any target and 3 damage to itself.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(SequenceEffect.simultaneous(DealDamageEffect.toAnyTarget(2), DealDamageEffect.to(Subjects.source(), 3))),
                "{T}: Psionic Entity deals 2 damage to any target and 3 damage to itself."
        ));
    }
}
