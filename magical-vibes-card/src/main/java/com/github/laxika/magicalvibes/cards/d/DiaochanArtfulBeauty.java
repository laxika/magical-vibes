package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PTK", collectorNumber = "108")
public class DiaochanArtfulBeauty extends Card {

    public DiaochanArtfulBeauty() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(DestroyTargetPermanentEffect.forTargetGroup(0),
                        DestroyTargetPermanentEffect.forTargetGroup(1)),
                "{T}: Destroy target creature of your choice, then destroy target creature of an opponent's choice. "
                        + "Activate only during your turn, before attackers are declared.",
                null,
                null,
                null,
                ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED,
                List.of(TargetFilters.creature(), TargetFilters.creature()), 2, 2
        ).withOpponentChosenTargetByController(1, TargetFilters.creature()).withAllowSharedTargets());
    }
}
