package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtRandomCardInTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "406")
public class UrzasBauble extends Card {

    public UrzasBauble() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new SacrificeSelfCost(),
                        new LookAtRandomCardInTargetPlayerHandEffect(),
                        new RegisterDrawCardsAtNextUpkeepEffect()),
                "{T}, Sacrifice Urza's Bauble: Look at a card at random in target player's hand. "
                        + "You draw a card at the beginning of the next turn's upkeep."));
    }
}
