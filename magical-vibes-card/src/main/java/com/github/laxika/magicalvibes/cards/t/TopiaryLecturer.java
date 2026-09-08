package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "165")
public class TopiaryLecturer extends Card {

    public TopiaryLecturer() {
        // Increment is driven automatically by the Scryfall-loaded INCREMENT keyword.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN, new SourcePower())),
                "{T}: Add an amount of {G} equal to this creature's power."
        ));
    }
}
