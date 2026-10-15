package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleSelfPowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "115")
public class CaseyJonesAsphaltHooligan extends Card {

    public CaseyJonesAsphaltHooligan() {
        // {4}: Double Casey Jones's power until end of turn. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(DoubleSelfPowerToughnessEffect.power()),
                "{4}: Double Casey Jones's power until end of turn. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
