package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "88")
public class InformationDealer extends Card {

    public InformationDealer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ReorderTopCardsOfLibraryEffect(
                        new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.WIZARD),
                                CountScope.ANY_PLAYER))),
                "{T}: Look at the top X cards of your library, where X is the number of Wizards on the battlefield, then put them back in any order."
        ));
    }
}
