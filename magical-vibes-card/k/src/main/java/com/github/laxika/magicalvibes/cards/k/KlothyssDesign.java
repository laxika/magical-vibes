package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.DevotionToColor;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "THB", collectorNumber = "176")
public class KlothyssDesign extends Card {

    public KlothyssDesign() {
        DevotionToColor greenDevotion = new DevotionToColor(ManaColor.GREEN);
        addSpellEffect(new BoostAllOwnCreaturesEffect(greenDevotion, greenDevotion));
    }
}
