package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect;

@CardRegistration(set = "SOM", collectorNumber = "72")
public class NecroticOoze extends Card {

    public NecroticOoze() {
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect());
    }
}
