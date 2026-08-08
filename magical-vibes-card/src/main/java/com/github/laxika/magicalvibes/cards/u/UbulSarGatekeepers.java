package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DGM", collectorNumber = "30")
public class UbulSarGatekeepers extends Card {

    public UbulSarGatekeepers() {
        // When this creature enters, if you control two or more Gates, target creature an opponent
        // controls gets -2/-2 until end of turn. Intervening-if gate (CR 603.4): the Gate count is
        // checked as the trigger goes on the stack and again at resolution, so the target is chosen
        // at trigger time rather than at cast time.
        target(TargetFilters.creatureAnOpponentControls()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(
                        new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.GATE)),
                        new BoostTargetCreatureEffect(-2, -2)));
    }
}
