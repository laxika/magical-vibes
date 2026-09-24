package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "965")
@CardRegistration(set = "SLD", collectorNumber = "1621")
public class GrenzoHavocRaiser extends Card {

    public GrenzoHavocRaiser() {
        PermanentPredicate defendingPlayerCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledByDefendingPlayerPredicate()));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null, new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Goad target creature",
                                new GoadTargetCreatureUntilNextTurnEffect(),
                                new PermanentPredicateTargetFilter(
                                        defendingPlayerCreature,
                                        "Target must be a creature defending player controls")),
                        new ChooseOneEffect.ChooseOneOption(
                                "Exile the top card",
                                new ExileTopCardOfDamagedPlayerLibraryMayCastThisTurnEffect())))));
    }
}
