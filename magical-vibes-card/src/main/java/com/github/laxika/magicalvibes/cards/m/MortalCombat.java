package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "10E", collectorNumber = "160")
@CardRegistration(set = "TOR", collectorNumber = "71")
public class MortalCombat extends Card {

    public MortalCombat() {
        // At the beginning of your upkeep, if twenty or more creature cards are in your graveyard,
        // you win the game. Intervening "if" (CR 603.4): checked on trigger and again on resolution.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new GraveyardCardThreshold(20, new CardTypePredicate(CardType.CREATURE)),
                new WinGameEffect()));
    }
}
