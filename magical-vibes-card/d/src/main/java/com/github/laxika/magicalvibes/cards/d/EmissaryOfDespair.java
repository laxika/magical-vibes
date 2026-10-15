package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "DST", collectorNumber = "42")
public class EmissaryOfDespair extends Card {

    public EmissaryOfDespair() {
        // Whenever this creature deals combat damage to a player, that player loses 1 life for each
        // artifact they control. The combat-damaged player is bound as the trigger's target, so the
        // TARGET_PLAYER count scope reads that player's artifacts.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new LoseLifeEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.TARGET_PLAYER),
                LoseLifeRecipient.TARGET_PLAYER));
    }
}
