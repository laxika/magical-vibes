package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C19", collectorNumber = "44")
public class GrismoldTheDreadsower extends Card {

    public GrismoldTheDreadsower() {
        // At the beginning of your end step, each player creates a 1/1 green Plant creature token.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new EachPlayerCreatesTokenEffect(
                new CreateTokenEffect(1, "Plant", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.PLANT), Set.of(), Set.of())));

        // Whenever a creature token dies, put a +1/+1 counter on Grismold.
        addTrigger(Triggers.creatureDies(new PermanentIsTokenPredicate(), ObjectRelation.ANY),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
