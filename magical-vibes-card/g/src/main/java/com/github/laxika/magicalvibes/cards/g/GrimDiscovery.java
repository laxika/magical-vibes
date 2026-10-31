package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "91")
@CardRegistration(set = "DDR", collectorNumber = "51")
public class GrimDiscovery extends Card {

    public GrimDiscovery() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card from your graveyard to your hand",
                        ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(new CardTypePredicate(CardType.CREATURE))),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target land card from your graveyard to your hand",
                        ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(new CardTypePredicate(CardType.LAND))),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target creature card and target land card from your graveyard to your hand",
                        ReturnTargetCardsFromGraveyardToHandEffect.oneOfEachCardType(CardType.CREATURE, CardType.LAND))
        )));
    }
}
