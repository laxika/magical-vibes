package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "134")
public class DwarvenReinforcements extends Card {

    public DwarvenReinforcements() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2, "Dwarf Berserker", 2, 1, CardColor.RED,
                List.of(CardSubtype.DWARF, CardSubtype.BERSERKER), Set.of(), Set.of()));
        addCastingOption(new ForetellCast("{1}{R}"));
    }
}
