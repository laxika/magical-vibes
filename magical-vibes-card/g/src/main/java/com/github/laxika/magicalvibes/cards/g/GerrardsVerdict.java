package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsThenGainLifeForEachCardTypeEffect;

@CardRegistration(set = "APC", collectorNumber = "102")
@CardRegistration(set = "DMR", collectorNumber = "190")
public class GerrardsVerdict extends Card {

    public GerrardsVerdict() {
        addSpellEffect(new TargetPlayerDiscardsThenGainLifeForEachCardTypeEffect(2, CardType.LAND, 3));
    }
}
