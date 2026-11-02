package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "37")
public class SukiCourageousRescuer extends Card {

    public SukiCourageousRescuer() {
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES));
        addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER_YOU_CONTROL).onlyDuringYourTurn(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new CreateTokenEffect("Ally", 1, 1, CardColor.WHITE, List.of(CardSubtype.ALLY), Set.of(), Set.of()));
    }
}
