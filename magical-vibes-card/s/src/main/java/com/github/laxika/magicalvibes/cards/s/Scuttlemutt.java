package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "263")
@CardRegistration(set = "M20", collectorNumber = "238")
public class Scuttlemutt extends Card {

    public Scuttlemutt() {
        // {T}: Add one mana of any color.
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        // {T}: Target creature becomes the color or colors of your choice until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BecomeChosenColorsUntilEndOfTurnEffect()),
                "{T}: Target creature becomes the color or colors of your choice until end of turn.",
                TargetFilters.creature()
        ));
    }
}
