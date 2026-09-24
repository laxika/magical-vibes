package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCardsInAllGraveyardsEffect;

@CardRegistration(set = "SOM", collectorNumber = "72")
@CardRegistration(set = "SLD", collectorNumber = "133")
@CardRegistration(set = "2X2", collectorNumber = "83")
public class NecroticOoze extends Card {

    public NecroticOoze() {
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfCardsInAllGraveyardsEffect(CardType.CREATURE));
    }
}
