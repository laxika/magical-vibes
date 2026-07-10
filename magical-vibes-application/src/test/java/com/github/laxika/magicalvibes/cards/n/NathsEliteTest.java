package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NathsEliteTest extends BaseCardTest {

    private Permanent castNathsElite() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NathsElite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (ETB clash trigger placed)
        harness.passBothPriorities(); // resolve ETB clash effect

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Nath's Elite"))
                .findFirst().orElseThrow();
    }

    // ===== ETB clash — win puts a +1/+1 counter on it =====

    @Test
    @DisplayName("Winning the clash puts a +1/+1 counter on Nath's Elite")
    void wonClashAddsCounter() {
        // Higher mana value on top for player1 (Grizzly Bears MV 2 > Forest MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        Permanent elite = castNathsElite();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(elite.getEffectivePower()).isEqualTo(5);
        assertThat(elite.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Losing the clash leaves Nath's Elite without a counter")
    void lostClashAddsNoCounter() {
        // Lower mana value on top for player1 (Forest MV 0 < Grizzly Bears MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        Permanent elite = castNathsElite();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(elite.getEffectivePower()).isEqualTo(4);
        assertThat(elite.getEffectiveToughness()).isEqualTo(2);
    }

    // ===== Static — all creatures able to block must do so =====

    @Test
    @DisplayName("All able creatures must block Nath's Elite")
    void allAbleCreaturesMustBlock() {
        Permanent elite = attackingCreature(new NathsElite());
        gd.playerBattlefields.get(player1.getId()).add(elite);

        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

        prepareDeclareBlockers();

        // Only one blocker assigned — should fail because both must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        // Both blockers assigned — should succeed
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    private Permanent attackingCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }

    private Permanent readyCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
