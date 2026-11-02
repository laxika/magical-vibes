package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactThenDealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MBS", collectorNumber = "69")
@CardRegistration(set = "2XM", collectorNumber = "134")
public class KuldothaFlamefiend extends Card {

    public KuldothaFlamefiend() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new SacrificeArtifactThenDealDividedDamageEffect(4), "Sacrifice an artifact?"));
    }
}
