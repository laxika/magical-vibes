package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "279")
public class SapseepForest extends Card {

    public SapseepForest() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {G}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.GREEN)),
                "{T}: Add {G}."
        ));

        // {G}, {T}: You gain 1 life. Activate only if you control two or more green permanents.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new GainLifeEffect(1)),
                "{G}, {T}: You gain 1 life. Activate only if you control two or more green permanents."
        ).withRequiredControlledPermanents(
                new PermanentColorInPredicate(Set.of(CardColor.GREEN)), 2, "green permanents"));
    }
}
