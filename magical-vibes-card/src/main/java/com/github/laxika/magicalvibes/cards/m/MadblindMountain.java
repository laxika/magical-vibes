package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "274")
public class MadblindMountain extends Card {

    public MadblindMountain() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.RED)),
                "{T}: Add {R}."
        ));

        // {R}, {T}: Shuffle your library. Activate only if you control two or more red permanents.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new ShuffleLibraryEffect(false)),
                "{R}, {T}: Shuffle your library. Activate only if you control two or more red permanents."
        ).withRequiredControlledPermanents(
                new PermanentColorInPredicate(Set.of(CardColor.RED)), 2, "red permanents"));
    }
}
