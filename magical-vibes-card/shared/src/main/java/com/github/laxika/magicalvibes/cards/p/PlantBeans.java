package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;

public class PlantBeans extends Card {

    public PlantBeans() {
        addSpellEffect(new PlayAdditionalLandsEffect(1));
    }
}
