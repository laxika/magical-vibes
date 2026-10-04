package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "AKH", collectorNumber = "118")
@CardRegistration(set = "AKR", collectorNumber = "139")
public class BattlefieldScavenger extends Card {

    public BattlefieldScavenger() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SkipNextUntapEffect(TapUntapScope.SELF),
                "Exert Battlefield Scavenger as it attacks?"
        ));
        addEffect(EffectSlot.ON_CONTROLLER_EXERTS,
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
