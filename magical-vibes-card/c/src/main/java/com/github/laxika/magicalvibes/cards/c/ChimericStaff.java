package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "313")
@CardRegistration(set = "USG", collectorNumber = "288")
public class ChimericStaff extends Card {

    public ChimericStaff() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}",
                List.of(new AnimatePermanentsEffect(new XValue(), new XValue(),
                        List.of(CardSubtype.CONSTRUCT), Set.of(), null, Set.of(),
                        Subjects.source(), Duration.UNTIL_END_OF_TURN, null)),
                "{X}: Chimeric Staff becomes an X/X Construct artifact creature until end of turn."
        ));
    }
}
