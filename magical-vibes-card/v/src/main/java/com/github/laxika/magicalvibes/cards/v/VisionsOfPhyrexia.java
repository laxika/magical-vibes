package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerDidntPlayCardFromExileThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "156")
public class VisionsOfPhyrexia extends Card {

    public VisionsOfPhyrexia() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ExileTopCardMayPlayThisTurnEffect(false));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerDidntPlayCardFromExileThisTurn()),
                CreateTokenEffect.ofPowerstoneToken(new Fixed(1)));
    }
}
