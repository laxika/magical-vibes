package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "156")
public class StaffOfDomination extends Card {

    public StaffOfDomination() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{1}: Untap this artifact."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new GainLifeEffect(1)),
                "{2}, {T}: You gain 1 life."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET, TargetFilters.creature().predicate())),
                "{3}, {T}: Untap target creature.",
                TargetFilters.creature()
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET, TargetFilters.creature().predicate())),
                "{4}, {T}: Tap target creature.",
                TargetFilters.creature()
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new DrawCardEffect(1)),
                "{5}, {T}: Draw a card."
        ));
    }
}
