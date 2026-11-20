package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "FRF", collectorNumber = "146")
public class Wildcall extends Card {

    public Wildcall() {
        addSpellEffect(new ManifestTopCardEffect());
        addSpellEffect(new PutCounterOnTargetPermanentEffect(
                Subjects.thisWay(StepKind.MANIFEST), CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
    }
}
