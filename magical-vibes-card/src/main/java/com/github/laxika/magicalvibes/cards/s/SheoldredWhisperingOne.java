package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "NPH", collectorNumber = "73")
public class SheoldredWhisperingOne extends Card {

    public SheoldredWhisperingOne() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.BATTLEFIELD).filter(new CardTypePredicate(CardType.CREATURE)).build());
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.TARGET_PLAYER));
    }
}
