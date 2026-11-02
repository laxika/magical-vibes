package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "213")
@CardRegistration(set = "EMA", collectorNumber = "172")
@CardRegistration(set = "HA3", collectorNumber = "20")
public class HondenOfLifesWeb extends Card {

    public HondenOfLifesWeb() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CreateTokenEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER),
                "Spirit", 1, 1, null, List.of(CardSubtype.SPIRIT), Set.of(), Set.of()));
    }
}
