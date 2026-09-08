package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalDamageFromColorSpellsEffect;

import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "220")
public class SulfuricVapors extends Card {

    public SulfuricVapors() {
        addEffect(EffectSlot.STATIC, new AdditionalDamageFromColorSpellsEffect(Set.of(CardColor.RED), 1));
    }
}
