package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptainMarVellSpaceBorn.class, GrizzlyBears.class})
class CaptainMarVellSpaceBornTest extends BaseCardTest {

    @Test
    @DisplayName("Does not grant flash before an opponent casts a spell")
    void doesNotGrantFlashBeforeOpponentSpell() {
        harness.addToBattlefield(player1, new CaptainMarVellSpaceBorn());
        prepareOpponentTurn();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Grants flash to spells after an opponent casts a spell")
    void grantsFlashAfterOpponentCastsSpell() {
        harness.addToBattlefield(player1, new CaptainMarVellSpaceBorn());
        prepareOpponentTurn();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Grants flash while the opponent's first spell is still on the stack")
    void grantsFlashBeforeOpponentSpellResolves() {
        harness.addToBattlefield(player1, new CaptainMarVellSpaceBorn());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new CaptainMarVellSpaceBorn()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.castCreature(player2, 0);

        harness.setHand(player1, List.of(new CaptainMarVellSpaceBorn()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Counts an opponent's spell cast before Captain Mar-Vell entered")
    void countsSpellCastBeforeEntering() {
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new CaptainMarVellSpaceBorn()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new CaptainMarVellSpaceBorn());
        harness.setHand(player1, List.of(new CaptainMarVellSpaceBorn()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Flash permission ends when Captain Mar-Vell leaves the battlefield")
    void losesFlashWhenCaptainLeaves() {
        harness.addToBattlefield(player1, new CaptainMarVellSpaceBorn());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new CaptainMarVellSpaceBorn()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new CaptainMarVellSpaceBorn()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Casting your own spell does not enable Cosmic Awareness")
    void ownSpellDoesNotEnableFlash() {
        harness.addToBattlefield(player1, new CaptainMarVellSpaceBorn());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
