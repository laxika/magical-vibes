package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringSpellControllerEffect;

@CardRegistration(set = "USG", collectorNumber = "209")
public class Retromancer extends Card {

    public Retromancer() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                new DealDamageToTriggeringSpellControllerEffect(3));
    }
}
