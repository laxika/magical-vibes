package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;

@CardRegistration(set = "WTH", collectorNumber = "4")
public class AngelicRenewal extends Card {

    public AngelicRenewal() {
        // Whenever a creature is put into your graveyard from the battlefield, you may sacrifice
        // this enchantment. If you do, return that card to the battlefield.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new MayEffect(new SacrificeSelfThenEffect(new ReturnTriggeringCreatureToBattlefieldEffect()),
                        "Sacrifice Angelic Renewal to return the creature to the battlefield?"));
    }
}
