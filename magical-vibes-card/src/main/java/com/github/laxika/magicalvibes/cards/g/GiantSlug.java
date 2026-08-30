package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterChosenLandwalkAtNextUpkeepEffect;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "33")
@CardRegistration(set = "LEG", collectorNumber = "99")
public class GiantSlug extends Card {

    public GiantSlug() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new RegisterChosenLandwalkAtNextUpkeepEffect()),
                "{5}: At the beginning of your next upkeep, choose a basic land type. This creature "
                        + "gains landwalk of the chosen type until the end of that turn."
        ));
    }
}
