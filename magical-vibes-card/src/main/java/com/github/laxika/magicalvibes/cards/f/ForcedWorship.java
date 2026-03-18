package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "11")
public class ForcedWorship extends Card {

    public ForcedWorship() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackEffect());
        addActivatedAbility(new ActivatedAbility(false, "{2}{W}", List.of(new ReturnSelfToHandEffect()), "{2}{W}: Return Forced Worship to its owner's hand."));
    }
}
