package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "66")
public class DreamsOfTheDead extends Card {

    public DreamsOfTheDead() {
        // {1}{U}: Return target white or black creature card from your graveyard to the battlefield.
        // That creature gains "Cumulative upkeep {2}." If the creature would leave the battlefield,
        // exile it instead of putting it anywhere else.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{U}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardAnyOfPredicate(List.of(
                                        new CardColorPredicate(CardColor.WHITE),
                                        new CardColorPredicate(CardColor.BLACK)
                                ))
                        )))
                        .targetGraveyard(true)
                        .exileIfLeavesBattlefield(true)
                        .grantCumulativeUpkeepCost("{2}")
                        .build()),
                "{1}{U}: Return target white or black creature card from your graveyard to the battlefield. "
                        + "That creature gains \"Cumulative upkeep {2}.\" If the creature would leave the "
                        + "battlefield, exile it instead of putting it anywhere else."
        ));
    }
}
