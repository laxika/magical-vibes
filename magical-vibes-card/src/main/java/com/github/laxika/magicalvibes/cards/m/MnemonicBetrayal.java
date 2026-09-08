package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentsGraveyardsAndMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

@CardRegistration(set = "GRN", collectorNumber = "189")
public class MnemonicBetrayal extends Card {

    public MnemonicBetrayal() {
        addEffect(EffectSlot.SPELL, new ExileOpponentsGraveyardsAndMayCastThisTurnEffect());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
