package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "111")
public class GoblinFreerunner extends Card {

    public GoblinFreerunner() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{1}{R}")),
                new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()), false));
    }
}
