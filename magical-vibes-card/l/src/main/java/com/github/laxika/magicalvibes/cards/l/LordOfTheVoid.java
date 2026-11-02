package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfDamagedPlayerLibraryAndPutCreatureOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RVR", collectorNumber = "81")
public class LordOfTheVoid extends Card {

    public LordOfTheVoid() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileTopCardsOfDamagedPlayerLibraryAndPutCreatureOntoBattlefieldEffect(7));
    }
}
