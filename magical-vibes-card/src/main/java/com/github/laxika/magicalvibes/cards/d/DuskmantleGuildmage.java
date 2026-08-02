package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterOpponentGraveyardLifeLossThisTurnEffect;
import java.util.List;

/**
 * Duskmantle Guildmage — the first ability creates a turn-scoped delayed trigger that drains 1 life
 * for every card put into an opponent's graveyard from any zone; the second is a straightforward
 * targeted mill.
 */
@CardRegistration(set = "GTC", collectorNumber = "158")
public class DuskmantleGuildmage extends Card {

    public DuskmantleGuildmage() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}{B}",
                List.of(new RegisterOpponentGraveyardLifeLossThisTurnEffect()),
                "{1}{U}{B}: Whenever a card is put into an opponent's graveyard from anywhere this turn, "
                        + "that player loses 1 life."));

        addActivatedAbility(new ActivatedAbility(false, "{2}{U}{B}",
                List.of(new MillEffect(2, MillRecipient.TARGET_PLAYER)),
                "{2}{U}{B}: Target player mills two cards."));
    }
}
