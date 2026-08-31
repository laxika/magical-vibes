package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "253")
public class ThreeBowlsOfPorridge extends Card {

    private static final String COST = "{2}";

    public ThreeBowlsOfPorridge() {
        addActivatedAbility(new ActivatedAbility(
                true,
                COST,
                List.of(new DealDamageToTargetCreatureEffect(2)),
                "{2}, {T}: This artifact deals 2 damage to target creature."
        ).withMaxActivationsPerGame(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                COST,
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsCreaturePredicate())),
                "{2}, {T}: Tap target creature."
        ).withMaxActivationsPerGame(1));

        addActivatedAbility(new ActivatedAbility(
                true,
                COST,
                List.of(new SacrificeSelfEffect(), new GainLifeEffect(3)),
                "{2}, {T}: Sacrifice this artifact. You gain 3 life."
        ).withMaxActivationsPerGame(1));
    }
}
