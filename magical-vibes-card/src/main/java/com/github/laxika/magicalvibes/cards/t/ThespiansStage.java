package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetLandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "248")
public class ThespiansStage extends Card {

    public ThespiansStage() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {2}, {T}: This land becomes a copy of target land, except it has this ability.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new BecomeCopyOfTargetLandEffect()),
                "{2}, {T}: This land becomes a copy of target land, except it has this ability.",
                TargetFilters.land()
        ));
    }
}
