package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "139")
public class LavaclawReaches extends Card {

    public LavaclawReaches() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLACK, ManaColor.RED))),
                "{T}: Add {B} or {R}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{R}",
                List.of(
                        AnimatePermanentsEffect.withAnimatedColors(
                                2, 2, List.of(CardSubtype.ELEMENTAL), Set.of(),
                                Set.of(CardColor.BLACK, CardColor.RED)),
                        new GrantActivatedAbilityEffect(
                                new ActivatedAbility(false, "{X}",
                                        List.of(new BoostSelfEffect(new XValue(), new Fixed(0))),
                                        "{X}: This creature gets +X/+0 until end of turn."),
                                GrantScope.SELF, null, EffectDuration.UNTIL_END_OF_TURN)
                ),
                "{1}{B}{R}: Until end of turn, this land becomes a 2/2 black and red Elemental creature "
                        + "with \"{X}: This creature gets +X/+0 until end of turn.\" It's still a land."
        ));
    }
}
