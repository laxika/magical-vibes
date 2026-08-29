package com.github.laxika.magicalvibes.cards.m;

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

@CardRegistration(set = "TLA", collectorNumber = "273")
public class MistyPalmsOasis extends Card {

    public MistyPalmsOasis() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{4}, {T}, Sacrifice this land: Draw a card."
        ));
    }
}
