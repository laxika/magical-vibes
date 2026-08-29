package com.github.laxika.magicalvibes.cards.c;

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
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "134")
public class CreepingTarPit extends Card {

    public CreepingTarPit() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.BLUE, ManaColor.BLACK))),
                "{T}: Add {U} or {B}."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}{B}",
                List.of(
                        AnimatePermanentsEffect.withAnimatedColors(
                                3, 2, List.of(CardSubtype.ELEMENTAL), Set.of(),
                                Set.of(CardColor.BLUE, CardColor.BLACK)),
                        new MakeCreatureUnblockableEffect(true)),
                "{1}{U}{B}: Until end of turn, this land becomes a 3/2 blue and black Elemental creature. It's still a land. It can't be blocked this turn."
        ));
    }
}
