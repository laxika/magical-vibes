package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromNonSubtypeCreaturesUntilEndOfTurnEffect;

@CardRegistration(set = "ISD", collectorNumber = "34")
public class SpareFromEvil extends Card {

    public SpareFromEvil() {
        addEffect(EffectSlot.SPELL, new GrantProtectionFromNonSubtypeCreaturesUntilEndOfTurnEffect(CardSubtype.HUMAN));
    }
}
