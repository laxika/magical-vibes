package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "64")
@CardRegistration(set = "HBG", collectorNumber = "908")
public class PouncingShoreshark extends Card {

    public PouncingShoreshark() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.mutates(), new MayEffect(
                        ReturnToHandEffect.target(),
                        "Return target creature an opponent controls to its owner's hand?"));
    }
}
