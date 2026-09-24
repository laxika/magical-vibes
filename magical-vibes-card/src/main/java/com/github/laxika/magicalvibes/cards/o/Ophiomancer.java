package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HA6", collectorNumber = "3")
@CardRegistration(set = "SOC", collectorNumber = "118")
public class Ophiomancer extends Card {

    public Ophiomancer() {
        // At the beginning of each upkeep, if you control no Snakes, create a 1/1 black Snake
        // creature token with deathtouch.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCountAtMost(0, new PermanentHasSubtypePredicate(CardSubtype.SNAKE)),
                new CreateTokenEffect("Snake", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.SNAKE), Set.of(Keyword.DEATHTOUCH), Set.of())));
    }
}
