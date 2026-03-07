package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "151")
public class PristineTalisman extends Card {

    public PristineTalisman() {
        // {T}: Add {C}. You gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS), new GainLifeEffect(1)),
                "{T}: Add {C}. You gain 1 life."
        ));
    }
}
