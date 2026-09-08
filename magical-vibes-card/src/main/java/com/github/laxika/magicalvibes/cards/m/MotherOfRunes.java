package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "14")
public class MotherOfRunes extends Card {

    public MotherOfRunes() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GrantProtectionChoiceUntilEndOfTurnEffect()),
                "{T}: Target creature you control gains protection from the color of your choice until end of turn.",
                TargetFilters.creatureYouControl()
        ));
    }
}
