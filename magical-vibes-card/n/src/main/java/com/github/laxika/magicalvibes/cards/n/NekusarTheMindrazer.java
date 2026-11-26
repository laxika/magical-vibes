package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
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
        addTrigger(Triggers.drawStep(ActivePlayer.ANY), new DrawCardEffect(1, Subjects.triggeringPlayer()));

        // Whenever an opponent draws a card, Nekusar deals 1 damage to that player.
        addTrigger(Triggers.opponentDrawsCard(),
                DealDamageEffect.to(Subjects.triggeringPlayer(), 1));
    }
}
