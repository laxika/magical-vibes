package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "9ED", collectorNumber = "293")
@CardRegistration(set = "8ED", collectorNumber = "296")
@CardRegistration(set = "ULG", collectorNumber = "125")
@CardRegistration(set = "MPS", collectorNumber = "34")
@CardRegistration(set = "BRR", collectorNumber = "13")
public class DefenseGrid extends Card {

    public DefenseGrid() {
        addStaticEffect(new IncreaseSpellCostEffect(new CardTruePredicate(), 3,
                Subjects.except(Subjects.eachPlayer(), Subjects.activePlayer())));
    }
}
