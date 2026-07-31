package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "ALL", collectorNumber = "95")
public class HailStorm extends Card {

    public HailStorm() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(2, false, false, new PermanentIsAttackingPredicate()));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                1, false, false, new PermanentControlledBySourceControllerPredicate()));
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
    }
}
