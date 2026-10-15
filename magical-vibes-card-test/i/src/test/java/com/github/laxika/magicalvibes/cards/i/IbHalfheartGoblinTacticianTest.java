package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBaloth;
import com.github.laxika.magicalvibes.cards.g.GoblinSkycutter;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IbHalfheartGoblinTactician.class, GoblinSkycutter.class, DurkwoodBaloth.class, Mountain.class})
class IbHalfheartGoblinTacticianTest extends BaseCardTest {

    @Test
    @DisplayName("Each blocked Goblin damages only its own blockers")
    void multipleBlockedGoblinsDamageOnlyTheirOwnBlockers() {
        Permanent firstGoblin = addCreatureReady(player1, new GoblinSkycutter());
        Permanent secondGoblin = addCreatureReady(player1, new GoblinSkycutter());
        firstGoblin.setAttacking(true);
        secondGoblin.setAttacking(true);
        Permanent ib = addCreatureReady(player1, new IbHalfheartGoblinTactician());
        Permanent firstBlocker = addCreatureReady(player2, new DurkwoodBaloth());
        Permanent secondBlocker = addCreatureReady(player2, new DurkwoodBaloth());
        Permanent uninvolvedCreature = addCreatureReady(player2, new DurkwoodBaloth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(ib).doesNotContain(firstGoblin, secondGoblin);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstGoblin.getCard(), secondGoblin.getCard());
        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(4);
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(4);
        assertThat(uninvolvedCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Tapped Mountains can pay the cost before the tokens are created")
    void tappedMountainsAreSacrificedAsAnImmediateCost() {
        Permanent ib = addCreatureReady(player1, new IbHalfheartGoblinTactician());
        Permanent firstMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent secondMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        firstMountain.tap();
        secondMountain.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(ib).doesNotContain(firstMountain, secondMountain);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstMountain.getCard(), secondMountain.getCard());
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).hasSize(2);
    }

    @Test
    @DisplayName("Another Goblin that becomes blocked is sacrificed and damages each creature blocking it")
    void anotherBlockedGoblinIsSacrificedAndDamagesItsBlockers() {
        Permanent goblin = addCreatureReady(player1, new GoblinSkycutter());
        goblin.setAttacking(true);
        Permanent ib = addCreatureReady(player1, new IbHalfheartGoblinTactician());
        Permanent firstBlocker = addCreatureReady(player2, new DurkwoodBaloth());
        Permanent secondBlocker = addCreatureReady(player2, new DurkwoodBaloth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ib).doesNotContain(goblin);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(goblin.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstBlocker, secondBlocker);
        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(4);
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Ib does not trigger for itself becoming blocked")
    void doesNotTriggerForIbBecomingBlocked() {
        Permanent ib = addCreatureReady(player1, new IbHalfheartGoblinTactician());
        ib.setAttacking(true);
        addCreatureReady(player2, new DurkwoodBaloth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ib does not trigger for a non-Goblin becoming blocked")
    void doesNotTriggerForNonGoblinBecomingBlocked() {
        Permanent attacker = addCreatureReady(player1, new DurkwoodBaloth());
        attacker.setAttacking(true);
        addCreatureReady(player1, new IbHalfheartGoblinTactician());
        addCreatureReady(player2, new DurkwoodBaloth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ib does not trigger for an opponent's Goblin becoming blocked")
    void doesNotTriggerForOpponentsGoblinBecomingBlocked() {
        addCreatureReady(player1, new DurkwoodBaloth());
        addCreatureReady(player1, new IbHalfheartGoblinTactician());
        Permanent goblin = addCreatureReady(player2, new GoblinSkycutter());
        goblin.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Goblin token created by Ib also triggers when it becomes blocked")
    void createdGoblinTokenTriggersWhenBlocked() {
        Permanent ib = addCreatureReady(player1, new IbHalfheartGoblinTactician());
        harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ib), 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Goblin").getFirst();
        token.setSummoningSick(false);
        token.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DurkwoodBaloth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrificing two Mountains creates two Goblin tokens")
    void sacrificesTwoMountainsToCreateTwoGoblins() {
        Permanent ib = addCreatureReady(player1, new IbHalfheartGoblinTactician());
        Permanent firstMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent secondMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ib), 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ib).doesNotContain(firstMountain, secondMountain);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstMountain.getCard(), secondMountain.getCard());
        assertThat(findPermanents(player1, "Goblin")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getName()).isEqualTo("Goblin");
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
            assertThat(token.isTapped()).isFalse();
        });
    }

    @Test
    @DisplayName("Ib cannot be activated without two Mountains to sacrifice")
    void cannotActivateWithoutTwoMountains() {
        Permanent ib = addCreatureReady(player1, new IbHalfheartGoblinTactician());
        harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefieldAndReturn(player1, new DurkwoodBaloth());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(ib), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }
}
