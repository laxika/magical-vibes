package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SHM", collectorNumber = "260")
@CardRegistration(set = "SLD", collectorNumber = "9")
@CardRegistration(set = "SLD", collectorNumber = "2170")
public class ReaperKing extends Card {

    public ReaperKing() {
        // Other Scarecrow creatures you control get +1/+1.
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SCARECROW)));

        // Whenever another Scarecrow you control enters, destroy target permanent.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(new PermanentHasSubtypePredicate(CardSubtype.SCARECROW),
                        new DestroyTargetPermanentEffect(false)));
    }
}
