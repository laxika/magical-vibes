package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "NPH", collectorNumber = "73")
public class SheoldredWhisperingOne extends Card {

    public SheoldredWhisperingOne() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ReturnCardFromGraveyardEffect(GraveyardChoiceDestination.BATTLEFIELD, new CardTypePredicate(CardType.CREATURE)));
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new SacrificeCreatureEffect());
    }
}
