package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "276")
public class RadiantEssence extends Card {

    public RadiantEssence() {
        // This creature gets +1/+2 as long as an opponent controls a black permanent.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.BLACK))),
                new StaticBoostEffect(1, 2, GrantScope.SELF)));
    }
}
