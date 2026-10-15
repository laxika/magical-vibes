package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "73")
public class DrillBit extends Card {

    public DrillBit() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{B}")),
                new OpponentLostLifeThisTurn(1),
                false));
        addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(
                1, List.of(CardType.LAND), HandChoiceDestination.DISCARD));
    }
}
