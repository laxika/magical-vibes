package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "236")
public class VoltaicServant extends Card {

    public VoltaicServant() {
        // At the beginning of your end step, untap target artifact.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new UntapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsArtifactPredicate()));
    }
}
