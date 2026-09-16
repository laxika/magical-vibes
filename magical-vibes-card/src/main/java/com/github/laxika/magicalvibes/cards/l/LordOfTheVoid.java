package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfDamagedPlayerLibraryAndPutCreatureOntoBattlefieldEffect;

@CardRegistration(set = "RVR", collectorNumber = "81")
public class LordOfTheVoid extends Card {

    public LordOfTheVoid() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileTopCardsOfDamagedPlayerLibraryAndPutCreatureOntoBattlefieldEffect(7));
    }
}
