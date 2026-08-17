package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "177")
public class HouseGuildmage extends Card {

    public HouseGuildmage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(new SkipNextUntapEffect(TapUntapScope.TARGET)),
                "{1}{U}, {T}: Target creature doesn't untap during its controller's next untap step.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{B}",
                List.of(new SurveilEffect(2)),
                "{2}{B}, {T}: Surveil 2."
        ));
    }
}
