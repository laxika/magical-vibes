package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnDragonOfEachColorEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "174")
public class CallTheSpiritDragons extends Card {

    public CallTheSpiritDragons() {
        addStaticEffect(new StaticBoostEffect(
                0,
                0,
                Set.of(Keyword.INDESTRUCTIBLE),
                GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.DRAGON))));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new PutCountersOnDragonOfEachColorEffect());
    }
}
