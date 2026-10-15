package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AdantoVanguard;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BishopOfTheBloodstained.class, AdantoVanguard.class, LightningStrike.class})
class BishopOfTheBloodstainedTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger targets opponent and goes on stack")
    void etbTriggerGoesOnStack() {
        castBishop();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Bishop of the Bloodstained");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB with only Bishop on battlefield causes 1 life loss (counts itself)")
    void etbWithOnlyBishopCausesOneLifeLoss() {
        castBishop();
        resolveAllTriggers();

        // Bishop is itself a Vampire, so opponent loses 1 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB with additional Vampires causes more life loss")
    void etbWithAdditionalVampiresCausesMoreLifeLoss() {
        // Put two additional Vampires on the battlefield
        harness.addToBattlefield(player1, new AdantoVanguard());
        harness.addToBattlefield(player1, new AdantoVanguard());

        castBishop();
        resolveAllTriggers();

        // 2 Adanto Vanguards + Bishop itself = 3 Vampires, opponent loses 3 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Controller does not gain life (not a drain effect)")
    void controllerDoesNotGainLife() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new AdantoVanguard());

        castBishop();
        resolveAllTriggers();

        // Controller's life should stay at 10 (no gain)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        // Opponent loses 2 life (Adanto Vanguard + Bishop)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new BishopOfTheBloodstained()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castBishop();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Game log records life loss")
    void gameLogRecordsLifeLoss() {
        castBishop();
        resolveAllTriggers();

        assertThat(gameLogContains("loses 1 life")).isTrue();
    }

    @Test
    @DisplayName("Opposing Vampires do not contribute to life loss")
    void opposingVampiresAreNotCounted() {
        harness.addToBattlefield(player2, new AdantoVanguard());
        harness.addToBattlefield(player2, new BishopOfTheBloodstained());

        castBishop();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Vampires entering before resolution contribute to life loss")
    void countsVampiresAtResolution() {
        castBishop();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new AdantoVanguard());

        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Removing Bishop before resolution leaves its trigger counting remaining Vampires")
    void triggerResolvesAfterBishopDies() {
        harness.addToBattlefield(player1, new AdantoVanguard());
        castBishop();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Bishop of the Bloodstained"));
        harness.assertNotOnBattlefield(player1, "Bishop of the Bloodstained");

        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Removing the only Vampire before resolution causes no life loss")
    void noVampiresAtResolutionCausesNoLifeLoss() {
        castBishop();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Bishop of the Bloodstained"));
        harness.assertNotOnBattlefield(player1, "Bishop of the Bloodstained");

        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void castBishop() {
        harness.setHand(player1, List.of(new BishopOfTheBloodstained()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0, player2.getId());
    }
}
