package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "8ED", collectorNumber = "168")
@CardRegistration(set = "10E", collectorNumber = "184")
@CardRegistration(set = "9ED", collectorNumber = "167")
@CardRegistration(set = "M10", collectorNumber = "115")
@CardRegistration(set = "LEG", collectorNumber = "124")
@CardRegistration(set = "THB", collectorNumber = "121")
@CardRegistration(set = "DPA", collectorNumber = "35")
@CardRegistration(set = "SLD", collectorNumber = "1298")
public class UnderworldDreams extends Card {

    public UnderworldDreams() {
        addTrigger(Triggers.opponentDrawsCard(), new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER));
    }
}
