package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasTapActivatedAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "162")
public class MagewrightsStone extends Card {

    public MagewrightsStone() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET,
                        new PermanentHasTapActivatedAbilityPredicate())),
                "{1}, {T}: Untap target creature that has an activated ability with {T} in its cost.",
                TargetFilters.creature()
        ));
    }
}
