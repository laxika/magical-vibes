package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "TMT", collectorNumber = "35")
@CardRegistration(set = "TMT", collectorNumber = "212")
@CardRegistration(set = "TMT", collectorNumber = "283")
@CardRegistration(set = "TMT", collectorNumber = "293")
public class DonatelloGadgetMaster extends Card {

    public DonatelloGadgetMaster() {
        addSneak("{1}{U}");
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(), "Target must be an artifact you control"
        )).addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new CreateTokenCopyOfTargetPermanentEffect());
    }
}
