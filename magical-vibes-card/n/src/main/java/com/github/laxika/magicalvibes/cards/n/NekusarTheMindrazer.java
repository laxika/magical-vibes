package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "860")
@CardRegistration(set = "SLD", collectorNumber = "1301")
@CardRegistration(set = "SLD", collectorNumber = "2184")
@CardRegistration(set = "CMM", collectorNumber = "349")
@CardRegistration(set = "CMM", collectorNumber = "587")
@CardRegistration(set = "C13", collectorNumber = "201")
public class NekusarTheMindrazer extends Card {

    public NekusarTheMindrazer() {
        // At the beginning of each player's draw step, that player draws an additional card.
        addTrigger(Triggers.drawStep(ActivePlayer.ANY), new DrawCardForTargetPlayerEffect(1));

        // Whenever an opponent draws a card, Nekusar deals 1 damage to that player.
        addTrigger(Triggers.opponentDrawsCard(),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER));
    }
}
