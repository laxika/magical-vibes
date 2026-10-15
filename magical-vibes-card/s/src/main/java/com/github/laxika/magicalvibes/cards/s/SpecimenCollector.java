package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "64")
public class SpecimenCollector extends Card {

    public SpecimenCollector() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Squirrel", 1, 1, CardColor.GREEN, List.of(CardSubtype.SQUIRREL),
                        Set.of(), Set.of()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect("Crab", 0, 3, CardColor.BLUE, List.of(CardSubtype.CRAB),
                        Set.of(), Set.of()));

        var tokenYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsTokenPredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        target(new PermanentPredicateTargetFilter(tokenYouControl, "Target must be a token you control"))
                .addEffect(EffectSlot.ON_DEATH, new CreateTokenCopyOfTargetPermanentEffect());
    }
}
