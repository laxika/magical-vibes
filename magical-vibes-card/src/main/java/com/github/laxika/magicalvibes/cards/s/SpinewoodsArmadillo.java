package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "182")
public class SpinewoodsArmadillo extends Card {

    public SpinewoodsArmadillo() {
        CardAnyOfPredicate basicLandOrDesert = new CardAnyOfPredicate(List.of(
                CardPredicateUtils.basicLand(),
                new CardSubtypePredicate(CardSubtype.DESERT)));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new SearchLibraryEffect(basicLandOrDesert), new GainLifeEffect(3)),
                "{1}{G}, Discard this card: Search your library for a basic land card or a Desert card, "
                        + "reveal it, put it into your hand, then shuffle. You gain 3 life."
        ));
    }
}
