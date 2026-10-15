package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "164")
public class UnchainedBerserker extends Card {

    public UnchainedBerserker() {
        addEffect(EffectSlot.STATIC,
                new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(2, 0, GrantScope.SELF, new PermanentIsAttackingPredicate()));
    }
}
