package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "30a")
@CardRegistration(set = "FEM", collectorNumber = "30b")
@CardRegistration(set = "FEM", collectorNumber = "30c")
@CardRegistration(set = "FEM", collectorNumber = "179")
public class VodalianMage extends Card {

    public VodalianMage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new CounterUnlessPaysEffect(1)),
                "{U}, {T}: Counter target spell unless its controller pays {1}."
        ));
    }
}
