package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "275")
public class Purgatory extends Card {

    public Purgatory() {
        // Whenever a nontoken creature is put into your graveyard from the battlefield, exile that card.
        addTrigger(Triggers.dies(Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_OWN),
                new ExileTriggeringCreatureAndTrackWithSourceEffect());
        // At the beginning of your upkeep, you may pay {4} and 2 life. If you do, return a card
        // exiled with this enchantment to the battlefield.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayPayManaEffect("{4}", 2,
                        new ReturnCardExiledWithSourceToBattlefieldEffect(),
                        "Pay {4} and 2 life to return a card exiled with Purgatory to the battlefield?"));
    }
}
