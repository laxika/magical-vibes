package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromGraveyardOnBottomOfLibraryCost;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "106")
public class BattlefieldScrounger extends Card {

    public BattlefieldScrounger() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PutCardsFromGraveyardOnBottomOfLibraryCost(3),
                        new BoostSelfEffect(3, 3)),
                "Threshold — Put three cards from your graveyard on the bottom of your library: "
                        + "This creature gets +3/+3 until end of turn. Activate only once each turn.",
                1
        ).withActivationCondition(
                new GraveyardCardThreshold(7, null),
                "Activate only if there are seven or more cards in your graveyard."));
    }
}
