package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "313")
public class ChimericStaff extends Card {

    public ChimericStaff() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(new AnimateSelfEffect(List.of(CardSubtype.CONSTRUCT))),
                false,
                "{X}: Chimeric Staff becomes an X/X Construct artifact creature until end of turn."
        ));
    }
}
