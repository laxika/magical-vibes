package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Coven;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIC", collectorNumber = "9")
@CardRegistration(set = "MIC", collectorNumber = "47")
public class StalwartPathlighter extends Card {

    public StalwartPathlighter() {
        // Coven — At the beginning of combat on your turn, if you control three or more creatures
        // with different powers, creatures you control gain indestructible until end of turn.
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new Coven()),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ALL_OWN_CREATURES));
    }
}
