package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetAndAttachedMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "71")
public class PortalOfSanctuary extends Card {

    public PortalOfSanctuary() {
        // {1}, {T}: Return target creature you control and each Aura attached to it to their owners'
        // hands. Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new ReturnTargetAndAttachedMatchingToHandEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.AURA))),
                "{1}, {T}: Return target creature you control and each Aura attached to it to their owners' hands. Activate only during your turn.",
                TargetFilters.creatureYouControl(),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
