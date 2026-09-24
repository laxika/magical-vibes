package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnSnapshotEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "CMM", collectorNumber = "215")
@CardRegistration(set = "CMM", collectorNumber = "533")
public class DisruptDecorum extends Card {

    public DisruptDecorum() {
        addEffect(EffectSlot.SPELL, new GoadCreaturesUntilNextTurnSnapshotEffect(new PermanentNotPredicate(
                new PermanentControlledBySourceControllerPredicate())));
    }
}
