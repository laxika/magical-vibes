package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "DOM", collectorNumber = "236")
public class VoltaicServant extends Card {

    public VoltaicServant() {
        // At the beginning of your end step, untap target artifact.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new UntapTargetPermanentEffect(new PermanentIsArtifactPredicate()));
    }
}
