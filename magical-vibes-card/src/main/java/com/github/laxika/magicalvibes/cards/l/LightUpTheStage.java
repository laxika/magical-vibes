package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "107")
public class LightUpTheStage extends Card {

    public LightUpTheStage() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{R}")), new OpponentLostLifeThisTurn(1), false));
        addEffect(EffectSlot.SPELL, new ExileTopCardsMayPlayUntilNextTurnEffect(2));
    }
}
