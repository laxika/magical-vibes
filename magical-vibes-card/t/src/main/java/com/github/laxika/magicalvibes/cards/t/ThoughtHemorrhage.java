package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseNameRevealHandDamagePerCopyAndExileEffect;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "47")
public class ThoughtHemorrhage extends Card {

    public ThoughtHemorrhage() {
        addEffect(EffectSlot.SPELL, new ChooseNameRevealHandDamagePerCopyAndExileEffect(List.of(CardType.LAND), 3));
    }
}
