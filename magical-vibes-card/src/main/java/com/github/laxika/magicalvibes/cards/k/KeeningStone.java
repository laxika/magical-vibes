package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "219")
public class KeeningStone extends Card {

    public KeeningStone() {
        // {5}, {T}: Target player mills X cards, where X is the number of cards in that player's graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new MillEffect(new CardsInGraveyard(null, CountScope.TARGET_PLAYER), MillRecipient.TARGET_PLAYER)),
                "{5}, {T}: Target player mills X cards, where X is the number of cards in that player's graveyard."
        ));
    }
}
