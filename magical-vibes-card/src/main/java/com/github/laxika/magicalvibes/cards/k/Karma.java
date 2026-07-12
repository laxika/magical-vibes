package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "8ED", collectorNumber = "28")
public class Karma extends Card {

    public Karma() {
        // At the beginning of each player's upkeep, deal damage to that player equal to
        // the number of Swamps they control. EACH_UPKEEP_TRIGGERED sets the active player as target.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.TARGET_PLAYER),
                        DamageRecipient.TARGET_PLAYER));
    }
}
