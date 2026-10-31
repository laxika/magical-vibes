package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "11")
public class ForcedWorship extends Card {

    public ForcedWorship() {
        target(TargetFilters.creature()).addStaticEffect(new EnchantedCreatureCantAttackOrBlockEffect(true, false));
        addActivatedAbility(new ActivatedAbility(false, "{2}{W}", List.of(ReturnToHandEffect.self()), "{2}{W}: Return Forced Worship to its owner's hand."));
    }
}
