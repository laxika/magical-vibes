package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "YMID", collectorNumber = "9")
public class SigardianEvangel extends Card {

    private static final String NAME = "Sigardian Evangel";

    public SigardianEvangel() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConjureCardNamedIntoHandEffect(NAME, true));

        PermanentPredicate permanentYouDoNotControl = new PermanentNotPredicate(
                new PermanentControlledBySourceControllerPredicate());
        target(new PermanentPredicateTargetFilter(permanentYouDoNotControl,
                "Target must be a permanent you don't control"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
