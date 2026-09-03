package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "283")
public class SnarlingUndorak extends Card {

    private static final PermanentAllOfPredicate BEAST_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.BEAST)
    ));

    public SnarlingUndorak() {
        addMorph("{1}{G}{G}");

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new BoostTargetCreatureEffect(1, 1, BEAST_CREATURE)),
                "{2}{G}: Target Beast creature gets +1/+1 until end of turn.",
                new PermanentPredicateTargetFilter(BEAST_CREATURE, "Target must be a Beast creature")
        ));
    }
}
