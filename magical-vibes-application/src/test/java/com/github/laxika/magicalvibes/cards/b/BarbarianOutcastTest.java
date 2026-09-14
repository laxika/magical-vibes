package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RancidEarth;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarbarianOutcast.class, RancidEarth.class, Swamp.class})
class BarbarianOutcastTest extends BaseCardTest {

    @Test
    @DisplayName("Is sacrificed when its controller controls no Swamps")
    void sacrificedWhenControllerControlsNoSwamps() {
        harness.addToBattlefield(player1, new BarbarianOutcast());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Barbarian Outcast");
        harness.assertInGraveyard(player1, "Barbarian Outcast");
    }

    @Test
    @DisplayName("Survives while its controller controls a Swamp")
    void survivesWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new BarbarianOutcast());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Barbarian Outcast");
    }

    @Test
    @DisplayName("Opponent's Swamp does not satisfy the condition")
    void opponentSwampDoesNotCount() {
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player1, new BarbarianOutcast());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Barbarian Outcast");
        harness.assertInGraveyard(player1, "Barbarian Outcast");
    }

    @Test
    @DisplayName("Triggers when its controller loses their last Swamp")
    void triggersWhenLastSwampLeaves() {
        var swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addToBattlefield(player1, new BarbarianOutcast());

        harness.setHand(player1, List.of(new RancidEarth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, swamp.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Swamp");
        harness.assertNotOnBattlefield(player1, "Barbarian Outcast");
        harness.assertInGraveyard(player1, "Barbarian Outcast");
    }

    @Test
    @DisplayName("Still sacrifices if a Swamp enters after the state trigger fires")
    void stillSacrificesAfterSwampEnters() {
        harness.addToBattlefield(player1, new BarbarianOutcast());

        harness.runStateBasedActions();
        harness.addToBattlefield(player1, new Swamp());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Barbarian Outcast");
        harness.assertInGraveyard(player1, "Barbarian Outcast");
    }
}
