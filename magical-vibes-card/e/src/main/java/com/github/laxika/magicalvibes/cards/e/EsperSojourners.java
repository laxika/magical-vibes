package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "107")
public class EsperSojourners extends Card {

    public EsperSojourners() {
        // When this creature dies, you may tap or untap target permanent. The explicit any-permanent
        // filter overrides the death pipeline's creatures-only default so lands/artifacts stay legal.
        target(TargetFilters.permanent())
                .addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                        new TapOrUntapTargetPermanentEffect(),
                        "Tap or untap target permanent?"));

        // When you cycle this card, you may tap or untap target permanent. The trigger is separate
        // from the cycling draw, so cycling is never blocked by the absence of a target.
        target(TargetFilters.permanent())
                .addTrigger(Triggers.youCycleThisCard(), new MayEffect(
                        new TapOrUntapTargetPermanentEffect(),
                        "Tap or untap target permanent?"));

        addCycling("{2}{U}");
    }
}
