package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "104")
@CardRegistration(set = "AKR", collectorNumber = "167")
@CardRegistration(set = "CMM", collectorNumber = "244")
@CardRegistration(set = "CMM", collectorNumber = "545")
@CardRegistration(set = "CMM", collectorNumber = "678")
@CardRegistration(set = "MKC", collectorNumber = "158")
public class NehebTheEternal extends Card {

    public NehebTheEternal() {
        // Afflict 3 — whenever this creature becomes blocked, defending player loses 3 life
        // (once per becoming blocked, not per blocker).
        addTrigger(Triggers.becomesBlocked(), new LoseLifeEffect(3, LoseLifeRecipient.DEFENDING_PLAYER));

        // At the beginning of each of your postcombat main phases, add {R} for each 1 life
        // your opponents have lost this turn (damage counts as life loss).
        addTrigger(Triggers.postcombatMainPhase(ActivePlayer.YOU),
                new AwardManaEffect(ManaColor.RED, new LifeLostThisTurn(CountScope.OPPONENTS)));
    }
}
