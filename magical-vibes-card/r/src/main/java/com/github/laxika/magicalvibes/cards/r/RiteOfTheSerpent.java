package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "86")
public class RiteOfTheSerpent extends Card {

    public RiteOfTheSerpent() {
        // Destroy target creature. If that creature had a +1/+1 counter on it, create a 1/1 green
        // Snake creature token for its controller.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                EventStat.NONE,
                new CreateTokenEffect("Snake", 1, 1, CardColor.GREEN, List.of(CardSubtype.SNAKE), Set.of(), Set.of()),
                ThenEffectRecipient.TARGET_CONTROLLER,
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
