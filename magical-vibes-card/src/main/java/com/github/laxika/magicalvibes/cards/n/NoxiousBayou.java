package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "118")
public class NoxiousBayou extends Card {

    public NoxiousBayou() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.GREEN)),
                        new GivePoisonCountersEffect(1, PoisonRecipient.CONTROLLER)
                ),
                "{T}: Add {B} or {G}. You get a poison counter."
        ));
    }
}
