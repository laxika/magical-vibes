package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Chilling Chronicle — back face of Mysterious Tome.
 */
public class ChillingChronicle extends Card {

    public ChillingChronicle() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET), new TransformSelfEffect()),
                "{1}, {T}: Tap target nonland permanent. Transform this artifact.",
                TargetFilters.nonlandPermanent()
        ));
    }
}
