package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "159")
@CardRegistration(set = "MM3", collectorNumber = "215")
@CardRegistration(set = "GK2", collectorNumber = "24")
@CardRegistration(set = "RVR", collectorNumber = "250")
public class AzoriusSignet extends Card {

    public AzoriusSignet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.WHITE), new AwardManaEffect(ManaColor.BLUE)),
                "{1}, {T}: Add {W}{U}."
        ));
    }
}
