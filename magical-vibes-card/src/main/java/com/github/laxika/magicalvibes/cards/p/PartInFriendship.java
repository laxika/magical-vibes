package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCreatureToBattlefieldOrHandByManaValueEffect;

@CardRegistration(set = "HOB", collectorNumber = "134")
public class PartInFriendship extends Card {

    public PartInFriendship() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new OncePerTurnTriggerEffect(
                        new RevealUntilCreatureToBattlefieldOrHandByManaValueEffect()));
    }
}
