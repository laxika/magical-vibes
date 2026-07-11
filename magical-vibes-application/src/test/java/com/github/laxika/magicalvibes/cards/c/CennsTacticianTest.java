package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CennsTacticianTest extends BaseCardTest {

    // ===== Activated ability =====

    @Test
    @DisplayName("{W}, {T}: Put a +1/+1 counter on a target Soldier creature")
    void abilityPutsCounterOnSoldier() {
        // Cenn's Tactician is itself a Kithkin Soldier, so it is a legal target.
        Permanent tactician = addReadyCreature(player1, new CennsTactician());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, tactician.getId());
        harness.passBothPriorities();

        assertThat(tactician.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Soldier creature")
    void cannotTargetNonSoldier() {
        addReadyCreature(player1, new CennsTactician());
        Permanent bear = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Static: additional block for creatures with a +1/+1 counter =====

    @Test
    @DisplayName("A creature you control with a +1/+1 counter can block an additional creature")
    void counteredCreatureCanBlockTwo() {
        addReadyCreature(player2, new CennsTactician());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAttackers(2);

        beginBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A creature you control without a +1/+1 counter cannot block an additional creature")
    void uncounteredCreatureCannotBlockTwo() {
        addReadyCreature(player2, new CennsTactician());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        addAttackers(2);

        beginBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent atk = new Permanent(new GrizzlyBears());
            atk.setSummoningSick(false);
            atk.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atk);
        }
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
