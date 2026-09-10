package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "282")
public class EchoChamber extends Card {

    public EchoChamber() {
        // {4}, {T}: An opponent chooses target creature they control. Create a token that's a copy
        // of that creature. That token gains haste until end of turn. Exile the token at the
        // beginning of the next end step. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new CreateTokenCopyOfTargetPermanentEffect(true, true)),
                "{4}, {T}: An opponent chooses target creature they control. Create a token that's a copy of that creature. "
                        + "That token gains haste until end of turn. Exile the token at the beginning of the next end step. "
                        + "Activate only as a sorcery.",
                TargetFilters.creatureAnOpponentControls(), null, null,
                ActivationTimingRestriction.SORCERY_SPEED
        ).withOpponentChosenTargetByController(0, TargetFilters.creatureAnOpponentControls()));
    }
}
