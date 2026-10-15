package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "233")
public class JodahsCodex extends Card {

    public JodahsCodex() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new ReduceActivationCostEffect(new BasicLandTypesAmongControlledLands()),
                        new DrawCardEffect()
                ),
                "{5}, {T}: Draw a card. This ability costs {1} less to activate for each basic land type among lands you control."
        ));
    }
}
