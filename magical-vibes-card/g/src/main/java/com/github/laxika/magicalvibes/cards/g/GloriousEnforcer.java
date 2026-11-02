package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerHasMoreLifeThanAnOpponent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "14")
public class GloriousEnforcer extends Card {

    public GloriousEnforcer() {
        // At the beginning of each combat, if you have more life than an opponent, this creature
        // gains double strike until end of turn.
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY),
                new ConditionalEffect(
                        new ControllerHasMoreLifeThanAnOpponent(),
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF)));
    }
}
