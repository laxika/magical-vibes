package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "172")
public class Hydroform extends Card {

    public Hydroform() {
        // Target land becomes a 3/3 Elemental creature with flying until end of turn.
        // It's still a land.
        target(TargetFilters.land()).addSpellEffect(new AnimatePermanentsEffect(
                3, 3,
                List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.FLYING),
                null, Set.of(),
                GrantScope.TARGET, Duration.UNTIL_END_OF_TURN));
    }
}
