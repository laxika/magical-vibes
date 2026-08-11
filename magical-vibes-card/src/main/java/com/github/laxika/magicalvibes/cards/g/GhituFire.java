package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "144")
public class GhituFire extends Card {

    public GhituFire() {
        // You may cast this spell as though it had flash if you pay {2} more to cast it.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{X}{2}{R}")), null, true));

        // Ghitu Fire deals X damage to any target.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new XValue()));
    }
}
