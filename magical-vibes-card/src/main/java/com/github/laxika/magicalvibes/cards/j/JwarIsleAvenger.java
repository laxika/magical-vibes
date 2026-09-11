package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "58")
public class JwarIsleAvenger extends Card {

    public JwarIsleAvenger() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{2}{U}")),
                new ControllerCastAnotherSpellThisTurn(new CardTruePredicate()), false));
    }
}
