package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AscendingAven;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrandMelee.class, GlorySeeker.class, AscendingAven.class})
class GrandMeleeTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures must attack each combat")
    void allCreaturesMustAttack() {
        harness.addToBattlefield(player1, new GrandMelee());
        addCreatureReady(player1, new GlorySeeker());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Creatures controlled by an opponent must attack each combat")
    void opponentsCreaturesMustAttack() {
        harness.addToBattlefield(player1, new GrandMelee());
        addCreatureReady(player2, new GlorySeeker());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Creatures that cannot attack are not forced to attack")
    void creaturesThatCannotAttackAreExempt() {
        harness.addToBattlefield(player1, new GrandMelee());
        Permanent creature = addCreatureReady(player1, new GlorySeeker());
        creature.setSummoningSick(true);

        assertThatCode(() -> declareAttackers(player1, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Losing Grand Melee's abilities removes its attack requirement")
    void losingAllAbilitiesRemovesAttackRequirement() {
        Permanent melee = harness.addToBattlefieldAndReturn(player1, new GrandMelee());
        melee.setLosesAllAbilitiesUntilEndOfTurn(true);
        addCreatureReady(player1, new GlorySeeker());

        assertThatCode(() -> declareAttackers(player1, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("All creatures must block each combat")
    void allCreaturesMustBlock() {
        harness.addToBattlefield(player1, new GrandMelee());
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        Permanent blocker = addCreatureReady(player2, new GlorySeeker());
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Creatures that cannot block are not forced to block")
    void creaturesThatCannotBlockAreExempt() {
        harness.addToBattlefield(player1, new GrandMelee());
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        Permanent blocker = addCreatureReady(player2, new GlorySeeker());
        blocker.tap();
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to attack")
    void tappedCreaturesAreExemptFromAttacking() {
        harness.addToBattlefield(player1, new GrandMelee());
        addCreatureReady(player1, new GlorySeeker()).tap();

        assertThatCode(() -> declareAttackers(List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Every able creature must attack, not just one")
    void cannotLeaveAnAbleCreatureOutOfAttack() {
        harness.addToBattlefield(player1, new GrandMelee());
        Permanent first = addCreatureReady(player1, new GlorySeeker());
        addCreatureReady(player1, new GlorySeeker());

        assertThatThrownBy(() -> declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(first))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Summoning sickness does not exempt a creature from blocking")
    void summoningSickCreatureMustBlock() {
        harness.addToBattlefield(player2, new GrandMelee());
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        blocker.setSummoningSick(true);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Losing Grand Melee's abilities removes its block requirement")
    void losingAllAbilitiesRemovesBlockRequirement() {
        Permanent melee = harness.addToBattlefieldAndReturn(player1, new GrandMelee());
        melee.setLosesAllAbilitiesUntilEndOfTurn(true);
        addCreatureReady(player1, new GlorySeeker());
        addCreatureReady(player2, new GlorySeeker());
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Ground creatures are not forced to block flying attackers")
    void evasionMakesBlockingOptionalWhenNoLegalBlockExists() {
        harness.addToBattlefield(player1, new GrandMelee());
        addCreatureReady(player1, new AscendingAven());
        addCreatureReady(player2, new GlorySeeker());
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Every able defender must block even when another creature already blocks")
    void cannotLeaveAnAbleDefenderOutOfBlocks() {
        harness.addToBattlefield(player1, new GrandMelee());
        addCreatureReady(player1, new GlorySeeker());
        addCreatureReady(player2, new GlorySeeker());
        addCreatureReady(player2, new GlorySeeker());
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }
}
