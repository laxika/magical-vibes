package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({TheForetoldSoldier.class, GrizzlyBears.class})
class TheForetoldSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("It must be blocked when an able blocker exists")
    void mustBeBlockedIfAble() {
        Permanent soldier = addCreatureReady(player1, new TheForetoldSoldier());
        soldier.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("It can't be blocked by more than one creature")
    void cannotBeBlockedByTwoCreatures() {
        Permanent soldier = addCreatureReady(player1, new TheForetoldSoldier());
        soldier.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(soldier);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker), attackerIndex),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker), attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("A tapped creature does not force a block")
    void mayRemainUnblockedWhenNoCreatureCanBlock() {
        Permanent soldier = addCreatureReady(player1, new TheForetoldSoldier());
        soldier.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new TheForetoldSoldier());
        blocker.tap();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        assertThat(gd.findExiledCard(soldier.getCard().getId())).isNotNull();
        harness.assertOnBattlefield(player2, "The Foretold Soldier");
    }

    @Test
    @DisplayName("Combat damage exiles it face down as a foretold card")
    void combatDamageForetellsIt() {
        TheForetoldSoldier card = new TheForetoldSoldier();
        card.setOwnerId(player1.getId());
        Permanent soldier = addCreatureReady(player1, card);
        soldier.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        ExiledCardEntry exiled = gd.findExiledCard(soldier.getCard().getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(gd.foretoldCardIds).contains(soldier.getCard().getId());

        gd.turnNumber++;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, soldier.getCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Foretold Soldier");
        assertThat(gd.foretoldCardIds).doesNotContain(soldier.getCard().getId());
    }

    @Test
    @DisplayName("Foretelling from hand pays two mana and permits casting on a later turn")
    void foretellFromHand() {
        TheForetoldSoldier card = new TheForetoldSoldier();
        harness.setHand(player1, List.of(card));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId()).faceDown()).isTrue();
        assertThat(gd.foretoldCardIds).contains(card.getId());
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.GREEN, 1);
        gd.turnNumber++;
        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Foretold Soldier");
    }

    @Test
    @DisplayName("Damage-triggered foretell does not allow casting on the same turn")
    void cannotRecastOnDamageTurn() {
        Permanent soldier = addCreatureReady(player1, new TheForetoldSoldier());
        soldier.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, soldier.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(soldier.getCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Dealing damage as a blocker also exiles it face down")
    void blockingDamageForetellsIt() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent soldier = addCreatureReady(player2, new TheForetoldSoldier());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "The Foretold Soldier");
        assertThat(gd.findExiledCard(soldier.getCard().getId()).faceDown()).isTrue();
        assertThat(gd.foretoldCardIds).contains(soldier.getCard().getId());
    }

    @Test
    @DisplayName("Lethal simultaneous combat damage puts it in the graveyard before self-exile resolves")
    void lethalCombatDamageDoesNotForetellDeadSoldiers() {
        Permanent attacker = addCreatureReady(player1, new TheForetoldSoldier());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new TheForetoldSoldier());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "The Foretold Soldier");
        harness.assertInGraveyard(player2, "The Foretold Soldier");
        assertThat(gd.findExiledCard(attacker.getCard().getId())).isNull();
        assertThat(gd.findExiledCard(blocker.getCard().getId())).isNull();
        assertThat(gd.foretoldCardIds).doesNotContain(attacker.getCard().getId(), blocker.getCard().getId());
    }

    @Test
    @DisplayName("Only the owner can cast a soldier that dealt damage under another player's control")
    void stolenSoldierBecomesForetoldForItsOwner() {
        TheForetoldSoldier card = new TheForetoldSoldier();
        card.setOwnerId(player2.getId());
        Permanent soldier = addCreatureReady(player1, card);
        soldier.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(card.getId()).ownerId()).isEqualTo(player2.getId());
        gd.turnNumber++;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castFromExile(player2, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "The Foretold Soldier");
    }
}
