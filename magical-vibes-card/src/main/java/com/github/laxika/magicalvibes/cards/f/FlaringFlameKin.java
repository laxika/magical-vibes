package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Enchanted;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DIS", collectorNumber = "62")
public class FlaringFlameKin extends Card {

    public FlaringFlameKin() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Enchanted(),
                new StaticBoostEffect(2, 2, Set.of(Keyword.TRAMPLE), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Enchanted(),
                new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                "{R}",
                                List.of(new BoostSelfEffect(1, 0)),
                                "{R}: This creature gets +1/+0 until end of turn."),
                        GrantScope.SELF)));
    }
}
