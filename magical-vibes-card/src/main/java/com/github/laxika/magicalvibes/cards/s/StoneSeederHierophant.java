package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "184")
public class StoneSeederHierophant extends Card {

    public StoneSeederHierophant() {
        // Landfall — Whenever a land you control enters, untap this creature.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new UntapPermanentsEffect(TapUntapScope.SELF));

        // {T}: Untap target land.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new UntapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsLandPredicate())),
                "{T}: Untap target land.",
                TargetFilters.land()));
    }
}
