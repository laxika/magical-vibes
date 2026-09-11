package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetPlayerLibraryEffect;

@CardRegistration(set = "SCG", collectorNumber = "47")
public class RavenGuildMaster extends Card {

    public RavenGuildMaster() {
        addMorph("{2}{U}{U}");
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileTopCardsOfTargetPlayerLibraryEffect(10));
    }
}
