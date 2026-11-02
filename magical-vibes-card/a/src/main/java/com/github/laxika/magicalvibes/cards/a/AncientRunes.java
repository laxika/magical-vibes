package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "161")
public class AncientRunes extends Card {

    public AncientRunes() {
        // At the beginning of each player's upkeep, deal damage to that player equal to the
        // number of artifacts they control. Triggers.upkeep(ActivePlayer.ANY) sets the active player as target.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new DealDamageToPlayersEffect(
                        new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.TARGET_PLAYER),
                        DamageRecipient.ACTIVE_PLAYER));
    }
}
