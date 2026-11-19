package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "STX", collectorNumber = "255")
public class ExcavatedWall extends Card {

    public ExcavatedWall() {
        // {1}, {T}: Mill a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new MillEffect(1, Subjects.you())),
                "{1}, {T}: Mill a card."
        ));
    }
}
