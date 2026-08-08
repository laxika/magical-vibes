package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "60")
public class WalkerOfSecretWays extends Card {

    public WalkerOfSecretWays() {
        // Ninjutsu {1}{U}
        addNinjutsu("{1}{U}");

        // Whenever this creature deals combat damage to a player, look at that player's hand.
        // The damaged player is bound as the trigger's target by CombatDamageService.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new LookAtHandEffect());

        // {1}{U}: Return target Ninja you control to its owner's hand. Activate only during your turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{U}",
                List.of(ReturnToHandEffect.target()),
                "{1}{U}: Return target Ninja you control to its owner's hand. "
                        + "Activate only during your turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.NINJA),
                                new PermanentControlledBySourceControllerPredicate()
                        )),
                        "Target must be a Ninja you control"
                ),
                null, null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
