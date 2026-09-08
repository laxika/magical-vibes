package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "67a")
@CardRegistration(set = "FEM", collectorNumber = "67b")
@CardRegistration(set = "FEM", collectorNumber = "67c")
public class ElvishHunter extends Card {

    public ElvishHunter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G}",
                List.of(new SkipNextUntapEffect(TapUntapScope.TARGET)),
                "{1}{G}, {T}: Target creature doesn't untap during its controller's next untap step.",
                TargetFilters.creature()
        ));
    }
}
