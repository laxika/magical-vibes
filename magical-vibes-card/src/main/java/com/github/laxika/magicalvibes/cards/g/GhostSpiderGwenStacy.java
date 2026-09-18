package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "SPE", collectorNumber = "14")
public class GhostSpiderGwenStacy extends Card {

    public GhostSpiderGwenStacy() {
        addEffect(EffectSlot.ON_ATTACK, new DealDamageToPlayersEffect(
                new PermanentCount(new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER),
                DamageRecipient.DEFENDING_PLAYER));
    }
}
