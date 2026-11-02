package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "118")
@CardRegistration(set = "AKR", collectorNumber = "139")
public class BattlefieldScavenger extends Card {

    public BattlefieldScavenger() {
        addTrigger(Triggers.attacks(), new MayEffect(
                new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true),
                "Exert Battlefield Scavenger as it attacks?"
        ));
        addTrigger(Triggers.youExertCreature(),
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
