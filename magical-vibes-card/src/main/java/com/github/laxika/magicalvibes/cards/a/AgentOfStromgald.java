package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "64a")
@CardRegistration(set = "ALL", collectorNumber = "64b")
public class AgentOfStromgald extends Card {

    public AgentOfStromgald() {
        // {R}: Add {B}.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new AwardManaEffect(ManaColor.BLACK)),
                "{R}: Add {B}."
        ));
    }
}
