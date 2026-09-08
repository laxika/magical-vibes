package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "253")
public class RacersRing extends Card {

    public RacersRing() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}{G}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{2}{R}{G}, {T}, Sacrifice this land: Draw a card."
        ));
    }
}
