package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentCastThreeOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.effect.ExileEachTargetSpellEffect;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "57")
public class MindbreakTrap extends Card {

    public MindbreakTrap() {
        addCastingOption(new AlternateHandCast(List.of(), new OpponentCastThreeOrMoreSpellsThisTurn(), false));
        target(null, 0, 99).addEffect(EffectSlot.SPELL, new ExileEachTargetSpellEffect());
    }
}
