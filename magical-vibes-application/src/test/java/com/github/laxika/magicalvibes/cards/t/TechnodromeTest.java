package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Technodrome.class, Spellbook.class, Forest.class, GrizzlyBears.class})
class TechnodromeTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void stopBeforeCombatDamage() {
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS));
    }

    @Test
    @DisplayName("Cannot attack while its power is below six")
    void cannotAttackBelowSixPower() {
        addCreatureReady(player1, new Technodrome());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot block while its power is below six")
    void cannotBlockBelowSixPower() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new Technodrome());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Can attack when its power is six or greater")
    void canAttackAtSixPower() {
        Permanent attacker = addCreatureReady(player1, new Technodrome());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackers(player1, List.of(0));
        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can block when its power is six or greater")
    void canBlockAtSixPower() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new Technodrome());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing another artifact draws a card and adds a +1/+1 counter")
    void sacrificesArtifactDrawsAndAddsCounter() {
        Permanent technodrome = addCreatureReady(player1, new Technodrome());
        harness.addToBattlefield(player1, new Spellbook());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(technodrome.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(technodrome.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Cannot sacrifice Technodrome itself when no other artifact is available")
    void cannotActivateWithoutAnotherArtifact() {
        addCreatureReady(player1, new Technodrome());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: another artifact");
    }

    @Test
    @DisplayName("Sacrifice and tap are paid before the draw and counter resolve")
    void paysCostsBeforeResolution() {
        Permanent technodrome = addCreatureReady(player1, new Technodrome());
        harness.addToBattlefield(player1, new Spellbook());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, null, null);

        assertThat(technodrome.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        assertThat(technodrome.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(technodrome.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        addCreatureReady(player1, new Technodrome());
        harness.addToBattlefield(player2, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Spellbook");
    }

    @Test
    @DisplayName("A nonartifact creature cannot pay the sacrifice cost")
    void cannotSacrificeNonartifactCreature() {
        addCreatureReady(player1, new Technodrome());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new Technodrome());
        harness.addToBattlefield(player1, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Power five is still insufficient to attack")
    void cannotAttackAtFivePower() {
        Permanent technodrome = addCreatureReady(player1, new Technodrome());
        technodrome.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability still draws if Technodrome leaves before resolution")
    void drawsAfterSourceLeavesBattlefield() {
        Permanent technodrome = addCreatureReady(player1, new Technodrome());
        harness.addToBattlefield(player1, new Spellbook());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(technodrome);
        gd.playerGraveyards.get(player1.getId()).add(technodrome.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        harness.assertNotOnBattlefield(player1, "Technodrome");
        assertThat(technodrome.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
