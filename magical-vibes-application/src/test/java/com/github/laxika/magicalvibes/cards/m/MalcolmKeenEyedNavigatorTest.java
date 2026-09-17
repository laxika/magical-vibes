package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.cards.r.ReefPirates;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalcolmKeenEyedNavigator.class, ReefPirates.class, GrizzlyBears.class, HermeticStudy.class})
class MalcolmKeenEyedNavigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure when a Pirate deals combat damage to an opponent")
    void createsTreasureWhenPirateDealsCombatDamage() {
        addCreatureReady(player1, new MalcolmKeenEyedNavigator());
        declareAttackers(List.of(0));

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates only one Treasure when multiple Pirates deal damage to one opponent")
    void batchesMultiplePiratesDealingDamage() {
        addCreatureReady(player1, new MalcolmKeenEyedNavigator());
        addCreatureReady(player1, new ReefPirates());
        declareAttackers(List.of(0, 1));

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a Treasure when a Pirate deals noncombat damage to an opponent")
    void createsTreasureForNoncombatDamage() {
        Permanent malcolm = addCreatureReady(player1, new MalcolmKeenEyedNavigator());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(malcolm.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a Treasure when a non-Pirate deals damage")
    void doesNotTriggerForNonPirate() {
        addCreatureReady(player1, new MalcolmKeenEyedNavigator());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(bears.getId());

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Does not create a Treasure when a Pirate deals damage to its controller")
    void doesNotTriggerForDamageToController() {
        Permanent malcolm = addCreatureReady(player1, new MalcolmKeenEyedNavigator());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(malcolm.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }
}
