package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromTargetPermanentColorsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "14")
public class SamiteElder extends Card {

    public SamiteElder() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantProtectionFromTargetPermanentColorsUntilEndOfTurnEffect()),
                "{T}: Creatures you control gain protection from the colors of target permanent you control until end of turn.",
                TargetFilters.permanentYouControl()));
    }
}
