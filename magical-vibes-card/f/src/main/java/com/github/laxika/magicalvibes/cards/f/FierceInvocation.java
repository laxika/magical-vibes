package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "FRF", collectorNumber = "98")
public class FierceInvocation extends Card {

    public FierceInvocation() {
        addSpellEffect(new ManifestTopCardEffect());
        addSpellEffect(new PutCounterOnTargetPermanentEffect(
                Subjects.thisWay(StepKind.MANIFEST), CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
