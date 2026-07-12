package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "295")
public class CrystalRod extends Card {

    public CrystalRod() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.BLUE), List.of(new GainLifeEffect(1))),
                "Gain 1 life?"));
    }
}
