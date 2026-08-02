package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "TMP", collectorNumber = "161")
public class AncientRunes extends Card {

    public AncientRunes() {
        // At the beginning of each player's upkeep, deal damage to that player equal to the
        // number of artifacts they control. EACH_UPKEEP_TRIGGERED sets the active player as target.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(
                        new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.TARGET_PLAYER),
                        DamageRecipient.TARGET_PLAYER));
    }
}
