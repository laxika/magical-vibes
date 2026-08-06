package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "172")
public class Hydroform extends Card {

    public Hydroform() {
        // Target land becomes a 3/3 Elemental creature with flying until end of turn.
        // It's still a land.
        target(TargetFilters.land()).addEffect(EffectSlot.SPELL, new AnimatePermanentsEffect(
                3, 3,
                List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.FLYING),
                null, Set.of(),
                GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN));
    }
}
