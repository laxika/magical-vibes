package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "115")
public class CaseyJonesAsphaltHooligan extends Card {

    public CaseyJonesAsphaltHooligan() {
        // {4}: Double Casey Jones's power until end of turn. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new BoostSelfEffect(new SourcePower(), new Fixed(0))),
                "{4}: Double Casey Jones's power until end of turn. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
