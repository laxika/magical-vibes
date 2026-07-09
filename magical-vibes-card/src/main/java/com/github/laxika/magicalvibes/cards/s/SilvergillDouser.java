package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "87")
public class SilvergillDouser extends Card {

    public SilvergillDouser() {
        // {T}: Target creature gets -X/-0 until end of turn, where X is the number of
        // Merfolk and/or Faeries you control.
        PermanentCount merfolkOrFaeries = new PermanentCount(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.MERFOLK),
                        new PermanentHasSubtypePredicate(CardSubtype.FAERIE)
                )),
                CountScope.CONTROLLER);

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostTargetCreatureEffect(new Scaled(merfolkOrFaeries, -1), new Fixed(0))),
                "{T}: Target creature gets -X/-0 until end of turn, where X is the number of Merfolk and/or Faeries you control."
        ));
    }
}
