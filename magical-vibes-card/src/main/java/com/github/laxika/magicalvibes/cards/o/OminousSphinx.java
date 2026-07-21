package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "41")
public class OminousSphinx extends Card {

    public OminousSphinx() {
        // Flying is auto-loaded from Scryfall.
        // Whenever you cycle or discard a card, target creature an opponent controls gets -2/-0 until
        // end of turn. Cycling discards the card (CR 702.29e), so the single "controller discards"
        // trigger covers both wordings. The effect's own filter ("a creature not controlled by the
        // ability's controller" = an opponent's creature) drives the target choice via
        // DiscardTriggerCollectorService's DiscardControllerTriggerTarget pipeline; no cast-time
        // target() is declared, so casting Ominous Sphinx itself never prompts for a target.
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new BoostTargetCreatureEffect(-2, 0, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())))));
    }
}
