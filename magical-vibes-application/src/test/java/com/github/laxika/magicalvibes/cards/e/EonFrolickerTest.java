package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EonFrolicker.class, JaceBeleren.class, Shock.class})
class EonFrolickerTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, gives the target opponent an extra turn and protects you and your planeswalkers")
    void castEtbGrantsExtraTurnAndProtection() {
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        castEon(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player2.getId());
        assertThat(gd.playerProtectionFromPlayerIdsUntilNextTurn.get(player1.getId()))
                .containsExactly(player2.getId());
        assertThat(gqs.playerHasProtectionFromOpponents(gd, player1.getId(), player2.getId())).isTrue();
        assertThat(gqs.hasProtectionFromOpponents(gd, jace, player2.getId())).isTrue();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThatThrownBy(() -> harness.castInstant(player2, 0, jace.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("The protection ends at the controller's next turn")
    void protectionEndsAtNextTurn() {
        harness.addToBattlefield(player1, new JaceBeleren());
        castEon(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gd.playerProtectionFromPlayerIdsUntilNextTurn)
                .doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("An Eon Frolicker that was not cast does not create the extra turn effect")
    void etbWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player1, new EonFrolicker());

        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.playerProtectionFromPlayerIdsUntilNextTurn).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("The ETB cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new EonFrolicker()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void castEon(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new EonFrolicker()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targetId);
    }
}
