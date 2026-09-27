package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourcePermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerOtherThanSourceOwnerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "C14", collectorNumber = "55")
public class CrownOfDoom extends Card {

    public CrownOfDoom() {
        // Whenever a creature attacks you or a planeswalker you control, it gets +2/+0 until end of turn.
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU, new BoostTargetCreatureEffect(2, 0));

        // {2}: Target player other than this artifact's owner gains control of it. Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new TargetPlayerGainsControlOfSourcePermanentUntilEndOfTurnEffect()),
                "{2}: Target player other than this artifact's owner gains control of Crown of Doom.",
                new PlayerPredicateTargetFilter(
                        new PlayerOtherThanSourceOwnerPredicate(),
                        "Target must be a player other than this artifact's owner"
                ),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
