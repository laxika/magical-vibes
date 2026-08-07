package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ExileDamageSourcePermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

/**
 * Hixus, Prison Warden — flash 4/4 whose combat-damage trigger only works the turn it entered,
 * so it is meant to be flashed in as a blocker-less "gotcha" during the opponent's attack.
 */
@CardRegistration(set = "ORI", collectorNumber = "19")
public class HixusPrisonWarden extends Card {

    public HixusPrisonWarden() {
        // "Whenever a creature deals combat damage to you, if Hixus entered this turn, exile that
        // creature until Hixus leaves the battlefield." The intervening-"if" is re-checked on
        // resolution (CR 603.4).
        addEffect(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU,
                new ExileDamageSourcePermanentUntilSourceLeavesEffect(
                        new PermanentIsCreaturePredicate(), true, new SourceEnteredThisTurn()));
    }
}
