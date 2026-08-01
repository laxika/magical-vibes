package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VIS", collectorNumber = "116")
public class QuirionDruid extends Card {

    public QuirionDruid() {
        // {G}, {T}: Target land becomes a 2/2 green creature that's still a land.
        // (This effect lasts indefinitely.)
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new AnimatePermanentsEffect(
                        2, 2,
                        List.of(),
                        Set.of(),
                        CardColor.GREEN, Set.of(),
                        GrantScope.TARGET, EffectDuration.PERMANENT
                )),
                "{G}, {T}: Target land becomes a 2/2 green creature that's still a land.",
                TargetFilters.land()
        ));
    }
}
