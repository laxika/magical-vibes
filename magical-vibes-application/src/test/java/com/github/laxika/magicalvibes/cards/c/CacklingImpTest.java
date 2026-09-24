package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CacklingImp.class)
class CacklingImpTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability targeting a player puts it on the stack and taps the Imp")
    void activatingTargetingPlayerPutsOnStack() {
        Permanent imp = addReadyImp(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
        assertThat(imp.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Target player loses 1 life on resolution")
    void targetPlayerLosesOneLife() {
        addReadyImp(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        addReadyImp(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        addReadyImp(player1);
        Permanent permanentTarget = addReadyImp(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, permanentTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a player");
    }

    @Test
    @DisplayName("Cannot target a player with protection from black")
    void cannotTargetPlayerWithProtectionFromBlack() {
        addReadyImp(player1);
        gd.playerProtectionFromColorsUntilEndOfTurn
                .computeIfAbsent(player2.getId(), ignored -> new HashSet<>())
                .add(CardColor.BLACK);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    private Permanent addReadyImp(Player player) {
        return addCreatureReady(player, new CacklingImp());
    }
}
