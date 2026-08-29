package com.github.laxika.magicalvibes.cards.b;

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

@CardRegistration(set = "TLA", collectorNumber = "267")
public class BoilingRockPrison extends Card {

    public BoilingRockPrison() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{4}, {T}, Sacrifice Boiling Rock Prison: Draw a card."
        ));
    }
}
