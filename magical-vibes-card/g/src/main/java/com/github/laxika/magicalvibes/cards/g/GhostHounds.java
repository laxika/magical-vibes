package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "49")
public class GhostHounds extends Card {

    public GhostHounds() {
        // Whenever this creature blocks or becomes blocked by a white creature,
        // this creature gains first strike until end of turn.
        PermanentColorInPredicate white = new PermanentColorInPredicate(Set.of(CardColor.WHITE));
        addTrigger(Triggers.blocks().withFilter(white), new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF));
        addTrigger(Triggers.becomesBlocked().withFilter(white),
                TriggerOptions.EACH_BLOCKER,
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF));
    }
}
