package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardAndCollectEvidenceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MKM", collectorNumber = "137")
@CardRegistration(set = "MKM", collectorNumber = "406")
public class LamplightPhoenix extends Card {

    public LamplightPhoenix() {
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new ExileSourceCardFromGraveyardAndCollectEvidenceEffect(4, true),
                "Exile Lamplight Phoenix and collect evidence 4?"));
    }
}
