package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "75")
public class OrganGrinder extends Card {

    public OrganGrinder() {
        // {T}, Exile three cards from your graveyard: Target player loses 3 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ExileNCardsFromGraveyardCost(3, null),
                        new LoseLifeEffect(3, LoseLifeRecipient.TARGET_PLAYER)
                ),
                "{T}, Exile three cards from your graveyard: Target player loses 3 life."
        ));
    }
}
