package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "328")
@CardRegistration(set = "PIP", collectorNumber = "295")
@CardRegistration(set = "PIP", collectorNumber = "513")
@CardRegistration(set = "PIP", collectorNumber = "823")
@CardRegistration(set = "PIP", collectorNumber = "1041")
public class SungrassPrairie extends Card {

    public SungrassPrairie() {
        // {1}, {T}: Add {G}{W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.GREEN), new AwardManaEffect(ManaColor.WHITE)),
                "{1}, {T}: Add {G}{W}."
        ));
    }
}
