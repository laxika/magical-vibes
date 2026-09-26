package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect;

@CardRegistration(set = "HOC", collectorNumber = "33")
@CardRegistration(set = "HOC", collectorNumber = "73")
@CardRegistration(set = "LTC", collectorNumber = "57")
@CardRegistration(set = "LTC", collectorNumber = "140")
public class GrMaSarumansFootman extends Card {

    public GrMaSarumansFootman() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect());
    }
}
