package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "133")
public class EmberheartChallenger extends Card {

    public EmberheartChallenger() {
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(),
                new TriggeringSpellControllerConditionalEffect(new OncePerTurnTriggerEffect(
                        new ExileTopCardMayPlayThisTurnEffect(false))));
    }
}
