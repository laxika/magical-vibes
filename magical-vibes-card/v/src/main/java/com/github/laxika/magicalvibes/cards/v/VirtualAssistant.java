package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastWithTeamworkPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "643")
public class VirtualAssistant extends Card {

    public VirtualAssistant() {
        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryCastWithTeamworkPredicate()),
                new CreateTokenEffect("Robot Hero Token", 1, 1, null,
                        List.of(CardSubtype.ROBOT, CardSubtype.HERO),
                        Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT)));
    }
}
