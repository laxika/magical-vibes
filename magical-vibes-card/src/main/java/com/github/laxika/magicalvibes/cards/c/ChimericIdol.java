package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PCY", collectorNumber = "136")
public class ChimericIdol extends Card {

    public ChimericIdol() {
        // {0}: Tap all lands you control. This artifact becomes a 3/3 Turtle artifact creature until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(
                        new TapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()),
                        new AnimatePermanentsEffect(3, 3, List.of(CardSubtype.TURTLE), Set.of(), null,
                                Set.of(CardType.ARTIFACT))
                ),
                "{0}: Tap all lands you control. This artifact becomes a 3/3 Turtle artifact creature until end of turn."
        ));
    }
}
