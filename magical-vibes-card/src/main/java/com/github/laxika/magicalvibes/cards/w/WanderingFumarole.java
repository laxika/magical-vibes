package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "182")
public class WanderingFumarole extends Card {

    public WanderingFumarole() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.RED))),
                "{T}: Add {U} or {R}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{R}",
                List.of(
                        AnimatePermanentsEffect.withAnimatedColors(
                                1, 4, List.of(CardSubtype.ELEMENTAL), Set.of(),
                                Set.of(CardColor.BLUE, CardColor.RED)),
                        new GrantActivatedAbilityEffect(
                                new ActivatedAbility(
                                        false,
                                        "{0}",
                                        List.of(new SwitchPowerToughnessEffect(true)),
                                        "{0}: Switch this creature's power and toughness until end of turn."
                                ),
                                GrantScope.SELF,
                                null,
                                EffectDuration.UNTIL_END_OF_TURN)
                ),
                "{2}{U}{R}: Until end of turn, this land becomes a 1/4 blue and red Elemental creature "
                        + "with \"{0}: Switch this creature's power and toughness until end of turn.\" It's still a land."
        ));
    }
}
