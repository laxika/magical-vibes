package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventionScope;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "15")
public class SamitePilgrim extends Card {

    public SamitePilgrim() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PreventDamageEffect(
                        PreventionScope.NEXT_TO_TARGET_CREATURE,
                        new BasicLandTypesAmongControlledLands(),
                        false,
                        null,
                        null,
                        null
                )),
                "{T}: Prevent the next X damage that would be dealt to target creature this turn, where X is the number of basic land types among lands you control."
        ));
    }
}
