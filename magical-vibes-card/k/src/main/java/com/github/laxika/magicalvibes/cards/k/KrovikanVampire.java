package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceDamagedCreatureDiedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ReturnDamagedCreaturesThatDiedUnderControlEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "141")
@CardRegistration(set = "ME2", collectorNumber = "102")
public class KrovikanVampire extends Card {

    public KrovikanVampire() {
        // At the beginning of each end step, if a creature dealt damage by this creature this turn
        // died, put that card onto the battlefield under your control. Sacrifice it when you lose
        // control of this creature.
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceDamagedCreatureDiedThisTurn()),
                new ReturnDamagedCreaturesThatDiedUnderControlEffect());
    }
}
