package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCreatureToBattlefieldRestToLibraryThenDemonPowerDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;

@CardRegistration(set = "40K", collectorNumber = "71")
public class AspiringChampion extends Card {

    public AspiringChampion() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new SacrificeSelfThenEffect(
                        new RevealUntilCreatureToBattlefieldRestToLibraryThenDemonPowerDamageEffect()));
    }
}
