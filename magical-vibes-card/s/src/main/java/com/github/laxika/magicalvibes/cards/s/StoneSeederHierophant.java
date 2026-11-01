package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "184")
public class StoneSeederHierophant extends Card {

    public StoneSeederHierophant() {
        // Landfall — Whenever a land you control enters, untap this creature.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
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
