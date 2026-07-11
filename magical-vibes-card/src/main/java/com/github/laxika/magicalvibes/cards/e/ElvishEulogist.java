package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "205")
public class ElvishEulogist extends Card {

    public ElvishEulogist() {
        // Sacrifice this creature: You gain 1 life for each Elf card in your graveyard.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new GainLifeEffect(new CardsInGraveyard(new CardSubtypePredicate(CardSubtype.ELF), CountScope.CONTROLLER))),
                "Sacrifice Elvish Eulogist: You gain 1 life for each Elf card in your graveyard."
        ));
    }
}
