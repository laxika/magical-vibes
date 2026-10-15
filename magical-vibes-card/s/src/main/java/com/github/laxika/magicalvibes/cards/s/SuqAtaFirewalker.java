package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "94")
public class SuqAtaFirewalker extends Card {

    public SuqAtaFirewalker() {
        addEffect(EffectSlot.STATIC, TargetingRestrictionEffect.fromSourceColors(Set.of(CardColor.RED)));
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(1)), "{T}: Suq'Ata Firewalker deals 1 damage to any target."));
    }
}
