package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "43")
public class RhysticDeluge extends Card {

    public RhysticDeluge() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(UnlessPaysEffect.unlessPays(Subjects.controllerOf(Subjects.target()), "{1}", new TapPermanentsEffect(Subjects.target()))
                        .withPrompt("Pay {1} to keep the target creature untapped?")),
                "{U}: Tap target creature unless its controller pays {1}.",
                TargetFilters.creature()
        ));
    }
}
