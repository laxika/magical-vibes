package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MKM", collectorNumber = "201")
public class EvidenceExaminer extends Card {

    public EvidenceExaminer() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new MayEffect(new CollectEvidenceEffect(4), "Collect evidence 4?"));
        addEffect(EffectSlot.ON_CONTROLLER_COLLECTS_EVIDENCE, CreateTokenEffect.ofClueToken(1));
    }
}
