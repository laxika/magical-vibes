package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromOpponentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C19", collectorNumber = "1")
public class CliffsideRescuer extends Card {

    public CliffsideRescuer() {
        // {T}, Sacrifice this creature: Target permanent you control gains protection from each of your opponents until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new GrantProtectionFromOpponentsUntilEndOfTurnEffect()),
                "{T}, Sacrifice Cliffside Rescuer: Target permanent you control gains protection from each of your opponents until end of turn.",
                TargetFilters.permanentYouControl()
        ));
    }
}
