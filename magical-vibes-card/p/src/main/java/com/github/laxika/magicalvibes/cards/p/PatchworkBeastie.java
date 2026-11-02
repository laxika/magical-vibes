package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "195")
public class PatchworkBeastie extends Card {

    public PatchworkBeastie() {
        addStaticEffect(new CantAttackOrBlockUnlessEffect(
                new Delirium(),
                "there are four or more card types among cards in your graveyard"
        ));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayEffect(new MillEffect(1, MillRecipient.CONTROLLER), "Mill a card?"));
    }
}
