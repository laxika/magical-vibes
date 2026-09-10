package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfDefendingPlayerLibraryEffect;

@CardRegistration(set = "BFZ", collectorNumber = "55")
public class BenthicInfiltrator extends Card {

    public BenthicInfiltrator() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ExileTopCardsOfDefendingPlayerLibraryEffect(1));
    }
}
