package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtCombatDamageToPlayerThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

/**
 * Excess — back half of Indulge // Excess.
 * Sorcery — Aftermath: create a Treasure token for each creature you controlled that dealt combat
 * damage to a player this turn.
 */
public class Excess extends Card {

    public Excess() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofTreasureToken(new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentDealtCombatDamageToPlayerThisTurnPredicate())),
                CountScope.CONTROLLER)));

        addCastingOption(new FlashbackCast("{1}{R}"));
    }
}
