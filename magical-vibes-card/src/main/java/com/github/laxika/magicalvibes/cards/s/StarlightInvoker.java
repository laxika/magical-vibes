package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

public class StarlightInvoker extends Card {

    public StarlightInvoker() {
        addActivatedAbility(new ActivatedAbility(false, "{7}{W}", List.of(new GainLifeEffect(5)), false, "{7}{W}: You gain 5 life."));
    }
}
