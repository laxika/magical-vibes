package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "83")
public class YueTheMoonSpirit extends Card {

    public YueTheMoonSpirit() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new WaterbendCost(5),
                        new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))
                ),
                "Waterbend {5}, {T}: You may cast a noncreature spell from your hand without paying its mana cost."
        ));
    }
}
