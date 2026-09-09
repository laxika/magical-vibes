package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutOpponentOwnedExiledCardIntoGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "56")
public class CrypticCruiser extends Card {

    public CrypticCruiser() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(
                        new PutOpponentOwnedExiledCardIntoGraveyardCost(),
                        new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{2}{U}, Put a card an opponent owns from exile into that player's graveyard: Tap target creature.",
                TargetFilters.creature()
        ));
    }
}
