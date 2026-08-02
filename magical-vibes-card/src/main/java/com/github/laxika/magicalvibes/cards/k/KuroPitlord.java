package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "123")
public class KuroPitlord extends Card {

    public KuroPitlord() {
        // At the beginning of your upkeep, sacrifice Kuro, Pitlord unless you pay {B}{B}{B}{B}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{B}{B}{B}{B}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // Pay 1 life: Target creature gets -1/-1 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PayLifeCost(1), new BoostTargetCreatureEffect(-1, -1)),
                "Pay 1 life: Target creature gets -1/-1 until end of turn.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(),
                        "Target must be a creature")));
    }
}
