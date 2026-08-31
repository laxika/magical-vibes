package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "ONS", collectorNumber = "1")
public class AkromasBlessing extends Card {

    public AkromasBlessing() {
        addEffect(EffectSlot.SPELL, new GrantProtectionChoiceUntilEndOfTurnEffect(GrantScope.OWN_CREATURES, null));
        addCycling("{W}");
    }
}
