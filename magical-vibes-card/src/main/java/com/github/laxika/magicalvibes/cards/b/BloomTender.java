package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardOneManaOfEachColorAmongControlledEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "66")
@CardRegistration(set = "MB1", collectorNumber = "66")
@CardRegistration(set = "2X2", collectorNumber = "138")
@CardRegistration(set = "ECL", collectorNumber = "166")
@CardRegistration(set = "ECL", collectorNumber = "324")
@CardRegistration(set = "ECL", collectorNumber = "390")
@CardRegistration(set = "ECL", collectorNumber = "400")
@CardRegistration(set = "SPG", collectorNumber = "79")
public class BloomTender extends Card {

    public BloomTender() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardOneManaOfEachColorAmongControlledEffect(new PermanentTruePredicate())),
                "Vivid — {T}: For each color among permanents you control, add one mana of that color."
        ));
    }
}
