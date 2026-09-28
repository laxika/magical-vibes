package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleControllerHandAndGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "190")
public class TrenzaloreClocktower extends Card {

    public TrenzaloreClocktower() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.BLUE),
                        new PutCountersOnSelfEffect(CounterType.TIME)
                ),
                "{T}: Add {U}. Put a time counter on Trenzalore Clocktower."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(
                        new RemoveCounterFromSourceCost(12, CounterType.TIME),
                        new ExileSelfCost(),
                        new ShuffleControllerHandAndGraveyardIntoLibraryEffect(),
                        new DrawCardEffect(7)
                ),
                "{1}{U}, {T}, Remove twelve time counters from Trenzalore Clocktower and exile it: Shuffle your graveyard and hand into your library, then draw seven cards. Activate only if you control a Time Lord."
        ).withRequiredControlledPermanents(
                new PermanentHasSubtypePredicate(CardSubtype.TIME_LORD),
                1,
                "a Time Lord"));
    }
}
