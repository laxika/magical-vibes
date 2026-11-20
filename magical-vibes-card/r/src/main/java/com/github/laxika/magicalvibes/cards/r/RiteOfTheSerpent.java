package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "86")
public class RiteOfTheSerpent extends Card {

    public RiteOfTheSerpent() {
        // Destroy target creature. If that creature had a +1/+1 counter on it, create a 1/1 green
        // Snake creature token for its controller.
        target(TargetFilters.creature()).addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new ConditionalEffect(new TargetPermanentMatches(new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)), new CreateTokenEffect("Snake", 1, 1, CardColor.GREEN, List.of(CardSubtype.SNAKE), Set.of(), Set.of())));
    }
}
