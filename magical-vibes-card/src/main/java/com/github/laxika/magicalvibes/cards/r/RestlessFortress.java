package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "259")
public class RestlessFortress extends Card {

    public RestlessFortress() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLACK))),
                "{T}: Add {W} or {B}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}{B}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        1, 4, List.of(CardSubtype.NIGHTMARE), Set.of(),
                        Set.of(CardColor.WHITE, CardColor.BLACK))),
                "{2}{W}{B}: This land becomes a 1/4 white and black Nightmare creature until end of turn. "
                        + "It's still a land."
        ));
        addEffect(EffectSlot.ON_ATTACK, new LoseLifeEffect(2, LoseLifeRecipient.DEFENDING_PLAYER));
        addEffect(EffectSlot.ON_ATTACK, new GainLifeEffect(2));
    }
}
