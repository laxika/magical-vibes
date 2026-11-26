package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "205")
@CardRegistration(set = "DKM", collectorNumber = "17")
@CardRegistration(set = "ME2", collectorNumber = "138")
public class OrcishCannoneers extends Card {

    public OrcishCannoneers() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(SequenceEffect.simultaneous(DealDamageEffect.toAnyTarget(2), DealDamageEffect.to(Subjects.you(),
                        3))),
                "{T}: Orcish Cannoneers deals 2 damage to any target and 3 damage to you."
        ));
    }
}
