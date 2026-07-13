package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "233")
public class OracleOfNectars extends Card {

    public OracleOfNectars() {
        // {X}, {T}: You gain X life.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new GainLifeEffect(new XValue())),
                "{X}, {T}: You gain X life."));
    }
}
