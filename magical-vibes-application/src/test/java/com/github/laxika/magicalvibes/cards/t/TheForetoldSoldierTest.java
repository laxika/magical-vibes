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
}
