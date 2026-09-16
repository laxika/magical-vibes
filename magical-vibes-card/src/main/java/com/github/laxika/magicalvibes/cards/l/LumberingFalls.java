package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PIO", collectorNumber = "268")
public class LumberingFalls extends Card {

    public LumberingFalls() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
        // {2}{G}{U}: Lumbering Falls becomes a 3/3 green and blue Elemental creature with hexproof until end of turn. It's still a land.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}{U}",
                List.of(AnimatePermanentsEffect.withAnimatedColors(
                        3, 3, List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HEXPROOF),
                        Set.of(CardColor.GREEN, CardColor.BLUE))),
                "{2}{G}{U}: Lumbering Falls becomes a 3/3 green and blue Elemental creature with hexproof until end of turn. It's still a land."
        ));
    }
}
