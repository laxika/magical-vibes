package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentExtraTurnSkipReplacementEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PZA", collectorNumber = "3")
@CardRegistration(set = "MKC", collectorNumber = "15")
@CardRegistration(set = "MKC", collectorNumber = "326")
public class TroubleInPairs extends Card {

    public TroubleInPairs() {
        addStaticEffect(new OpponentExtraTurnSkipReplacementEffect());

        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new OpponentAttacksWithAtLeastCreatures(2, false)),
                new DrawCardEffect());

        addTrigger(Triggers.opponentDrawsSecondCardEachTurn(),
                new DrawCardEffect());

        addTrigger(Triggers.opponentCastsSpell().nthThisTurn(2), new DrawCardEffect());
    }
}
