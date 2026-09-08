package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GravebornMuse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarangRiverProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast from graveyard while controlling a black permanent")
    void canCastFromGraveyardWithBlackPermanent() {
        harness.setGraveyard(player1, List.of(new MarangRiverProwler()));
        harness.addToBattlefield(player1, new GravebornMuse());
        addManaToCast();

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Can cast from graveyard while controlling a green permanent")
    void canCastFromGraveyardWithGreenPermanent() {
        harness.setGraveyard(player1, List.of(new MarangRiverProwler()));
        harness.addToBattlefield(player1, new LlanowarElves());
        addManaToCast();

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot cast from graveyard without a black or green permanent")
    void cannotCastFromGraveyardWithoutBlackOrGreenPermanent() {
        harness.setGraveyard(player1, List.of(new MarangRiverProwler()));
        addManaToCast();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Opponent's black permanent does not enable casting from graveyard")
    void opponentBlackPermanentDoesNotEnableGraveyardCast() {
        harness.setGraveyard(player1, List.of(new MarangRiverProwler()));
        harness.addToBattlefield(player2, new GravebornMuse());
        addManaToCast();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Cannot be blocked")
    void cannotBeBlocked() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent prowler = new Permanent(new MarangRiverProwler());
        prowler.setSummoningSick(false);
        prowler.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(prowler);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Cannot be declared as a blocker")
    void cannotBlock() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent prowler = new Permanent(new MarangRiverProwler());
        prowler.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(prowler);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaToCast() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
