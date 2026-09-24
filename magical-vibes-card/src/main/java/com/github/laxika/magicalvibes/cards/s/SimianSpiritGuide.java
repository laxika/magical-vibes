package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "122")
@CardRegistration(set = "A25", collectorNumber = "148")
@CardRegistration(set = "SLD", collectorNumber = "1548")
@CardRegistration(set = "TSR", collectorNumber = "190")
@CardRegistration(set = "SLZ", collectorNumber = "68")
@CardRegistration(set = "SLZ", collectorNumber = "189")
@CardRegistration(set = "SLZ", collectorNumber = "310")
public class SimianSpiritGuide extends Card {

    public SimianSpiritGuide() {
        addHandActivatedAbility(new ActivatedAbility(false, null,
                List.of(new AwardManaEffect(ManaColor.RED)),
                "Exile this card from your hand: Add {R}.")
                .withExilesSourceFromHand());
    }
}
