package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "69")
@CardRegistration(set = "TSR", collectorNumber = "108")
public class DeadlyGrub extends Card {

    public DeadlyGrub() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(3)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME));
        addTrigger(Triggers.dies(
                        new PermanentNotPredicate(new PermanentHasCountersPredicate(CounterType.TIME)),
                        ObjectRelation.THIS),
                new CreateTokenEffect("Insect", 6, 1, CardColor.GREEN, List.of(CardSubtype.INSECT), Set.of(Keyword.SHROUD), Set.of()));
    }
}
