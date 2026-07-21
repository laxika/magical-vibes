package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "183")
public class ShefetDunes extends Card {

    public ShefetDunes() {
        // {T}: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS)),
                "{T}: Add {C}."
        ));

        // {T}, Pay 1 life: Add {W}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(1), new AwardManaEffect(ManaColor.WHITE)),
                "{T}, Pay 1 life: Add {W}."
        ));

        // {2}{W}{W}, {T}, Sacrifice a Desert: Creatures you control get +1/+1 until end of turn.
        // Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}{W}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.DESERT),
                                "Sacrifice a Desert",
                                false),
                        new BoostAllOwnCreaturesEffect(1, 1)),
                "{2}{W}{W}, {T}, Sacrifice a Desert: Creatures you control get +1/+1 until end of turn. "
                        + "Activate only as a sorcery.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
