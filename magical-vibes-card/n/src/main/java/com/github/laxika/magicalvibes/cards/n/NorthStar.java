package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SpendAnyManaTypeForNextSpellThisTurnEffect;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "288")
public class NorthStar extends Card {

    public NorthStar() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new SpendAnyManaTypeForNextSpellThisTurnEffect()),
                "{4}, {T}: For one spell this turn, you may spend mana as though it were mana of any type to pay that spell's mana cost."
        ));
    }
}
