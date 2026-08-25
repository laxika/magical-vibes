package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "90")
public class FledglingDragon extends Card {

    public FledglingDragon() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new StaticBoostEffect(3, 3, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                false,
                                "{R}",
                                List.of(new BoostSelfEffect(1, 0)),
                                "{R}: This creature gets +1/+0 until end of turn."),
                        GrantScope.SELF)));
    }
}
