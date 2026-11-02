package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCreatureToBattlefieldRestToLibraryThenDemonPowerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "71")
public class AspiringChampion extends Card {

    public AspiringChampion() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new SacrificeSelfThenEffect(
                        new RevealUntilCreatureToBattlefieldRestToLibraryThenDemonPowerDamageEffect()));
    }
}
