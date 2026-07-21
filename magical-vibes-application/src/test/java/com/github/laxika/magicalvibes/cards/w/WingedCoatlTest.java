package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WingedCoatlTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Resolving Winged Coatl puts it onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new WingedCoatl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Winged Coatl");
    }

    // ===== Flash — instant-speed casting =====

    @Test
    @DisplayName("Can cast during the combat step thanks to Flash")
    void canCastDuringCombat() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new WingedCoatl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Winged Coatl");
    }

    @Test
    @DisplayName("Can cast during opponent's turn thanks to Flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new WingedCoatl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.getGameService().passPriority(gd, player2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Winged Coatl");
    }

    // ===== Flying — block restrictions =====

    @Test
    @DisplayName("Winged Coatl cannot be blocked by a creature without flying or reach")
    void cannotBeBlockedByGroundCreature() {
        Permanent coatl = new Permanent(new WingedCoatl());
        coatl.setSummoningSick(false);
        coatl.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(coatl);

        Permanent groundBlocker = new Permanent(new GrizzlyBears());
        groundBlocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(groundBlocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("Winged Coatl can be blocked by a flying creature")
    void canBeBlockedByFlyer() {
        Permanent coatl = new Permanent(new WingedCoatl());
        coatl.setSummoningSick(false);
        coatl.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(coatl);

        Permanent flyingBlocker = new Permanent(new AirElemental());
        flyingBlocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(flyingBlocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
        assertThat(flyingBlocker.isBlocking()).isTrue();
    }

    // ===== Deathtouch — combat interaction =====

    @Test
    @DisplayName("Winged Coatl's 1 deathtouch damage destroys a larger blocked creature")
    void deathtouchDestroysLargerCreature() {
        // Hill Giant (3/3) attacks; Winged Coatl (1/1 deathtouch, flying) blocks it.
        Permanent hillGiant = new Permanent(new HillGiant());
        hillGiant.setSummoningSick(false);
        hillGiant.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(hillGiant);

        Permanent coatl = new Permanent(new WingedCoatl());
        coatl.setSummoningSick(false);
        coatl.setBlocking(true);
        coatl.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(coatl);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // 1 deathtouch damage destroys the 3/3.
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
        // Winged Coatl (1 toughness) dies to the 3 damage it took back.
        harness.assertInGraveyard(player2, "Winged Coatl");
    }
}
