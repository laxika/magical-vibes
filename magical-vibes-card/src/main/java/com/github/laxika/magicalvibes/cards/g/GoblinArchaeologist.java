package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "63")
public class GoblinArchaeologist extends Card {

    public GoblinArchaeologist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new FlipCoinWinEffect(
                        SequenceEffect.of(
                                new DestroyTargetPermanentEffect(),
                                new UntapPermanentsEffect(TapUntapScope.SOURCE_PERMANENT)),
                        new SacrificeSelfEffect())),
                "{R}, {T}: Flip a coin. If you win the flip, destroy target artifact and untap this creature. "
                        + "If you lose the flip, sacrifice this creature.",
                TargetFilters.artifact()
        ));
    }
}
