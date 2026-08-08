package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

/**
 * {@code {T}: Add {G}.} plus "Whenever you tap this creature for mana, it deals 1 damage to each
 * opponent." The damage rider is a separate triggered ability, not part of the mana ability, so it
 * uses the stack once a player would next receive priority.
 */
@CardRegistration(set = "DGM", collectorNumber = "120")
public class ZhurTaaDruid extends Card {

    public ZhurTaaDruid() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
        addEffect(EffectSlot.ON_SELF_TAPPED_FOR_MANA,
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
    }
}
