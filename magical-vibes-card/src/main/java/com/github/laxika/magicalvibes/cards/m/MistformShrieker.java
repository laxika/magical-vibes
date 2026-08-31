package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesChosenSubtypeUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "96")
public class MistformShrieker extends Card {

    public MistformShrieker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SourceBecomesChosenSubtypeUntilEndOfTurnEffect()),
                "{1}: This creature becomes the creature type of your choice until end of turn."
        ));
    }
}
