package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "178")
@CardRegistration(set = "WHO", collectorNumber = "783")
public class LaserScrewdriver extends Card {

    public LaserScrewdriver() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{1}, {T}: Tap target artifact.",
                TargetFilters.artifact()
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new SurveilEffect(1)),
                "{2}, {T}: Surveil 1."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new GoadTargetCreatureUntilNextTurnEffect()),
                "{3}, {T}: Goad target creature.",
                TargetFilters.creature()
        ));
    }
}
