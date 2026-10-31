package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentsGraveyardsAndMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

@CardRegistration(set = "GRN", collectorNumber = "189")
public class MnemonicBetrayal extends Card {

    public MnemonicBetrayal() {
        addSpellEffect(new ExileOpponentsGraveyardsAndMayCastThisTurnEffect());
        addSpellEffect(new ExileSpellEffect());
    }
}
