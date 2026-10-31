package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.Set;

@CardRegistration(set = "M20", collectorNumber = "164")
public class UnchainedBerserker extends Card {

    public UnchainedBerserker() {
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));
        addStaticEffect(new StaticBoostEffect(2, 0, GrantScope.SELF, new PermanentIsAttackingPredicate()));
    }
}
