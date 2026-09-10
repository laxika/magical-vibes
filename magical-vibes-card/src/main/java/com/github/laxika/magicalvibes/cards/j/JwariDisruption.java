package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "64")
public class JwariDisruption extends Card {

    public JwariDisruption() {
        setBackFaceCard(new JwariRuins());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell unless its controller pays {1}",
                        new CounterUnlessPaysEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Jwari Ruins", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "JwariRuins";
    }
}
