package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Freerunning;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ACR", collectorNumber = "3")
public class DistractTheGuards extends Card {

    public DistractTheGuards() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{1}{W}")), new Freerunning(), false));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(3, "Human Rogue", 1, 1,
                CardColor.WHITE, List.of(CardSubtype.HUMAN, CardSubtype.ROGUE), Set.of(), Set.of()));
    }
}
