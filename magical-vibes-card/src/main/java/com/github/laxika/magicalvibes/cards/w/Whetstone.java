package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "316")
public class Whetstone extends Card {

    public Whetstone() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new MillEffect(2, MillRecipient.CONTROLLER),
                        new MillEffect(2, MillRecipient.EACH_OPPONENT)
                ),
                "{3}: Each player mills two cards."
        ));
    }
}
