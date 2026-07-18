package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "5ED", collectorNumber = "204")
@CardRegistration(set = "4ED", collectorNumber = "169")
public class WarpArtifact extends Card {

    public WarpArtifact() {
        // Enchant artifact. At the beginning of the upkeep of enchanted artifact's
        // controller, this Aura deals 1 damage to that player.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Target must be an artifact"
        )).addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(1, DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER));
    }
}
