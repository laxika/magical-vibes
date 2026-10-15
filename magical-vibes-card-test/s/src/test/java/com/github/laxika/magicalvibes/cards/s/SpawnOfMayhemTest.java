package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpawnOfMayhem.class, Mortify.class})
class SpawnOfMayhemTest extends BaseCardTest {

    @Test
    @DisplayName("Spectacle casts Spawn of Mayhem for {1}{B}{B} after an opponent loses life")
    void spectacleUsesAlternateCost() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new SpawnOfMayhem()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Spectacle is unavailable when no opponent has lost life")
    void spectacleRequiresOpponentLifeLoss() {
        harness.setHand(player1, List.of(new SpawnOfMayhem()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Upkeep damage hits each player and the post-damage life check adds a counter")
    void upkeepDealsDamageThenAddsCounterAtTenLife() {
        Permanent spawn = addCreatureReady(player1, new SpawnOfMayhem());
        harness.setLife(player1, 11);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(spawn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep damage does not add a counter while the controller remains above ten life")
    void upkeepDoesNotAddCounterAboveTenLife() {
        Permanent spawn = addCreatureReady(player1, new SpawnOfMayhem());
        harness.setLife(player1, 12);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(spawn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Normal casting remains available without opponent life loss")
    void normalCostDoesNotRequireLifeLoss() {
        harness.setHand(player1, List.of(new SpawnOfMayhem()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Spawn of Mayhem");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The controller's own life loss does not enable spectacle")
    void spectacleIgnoresControllerLifeLoss() {
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new SpawnOfMayhem()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spawn of Mayhem does not trigger during an opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        Permanent spawn = addCreatureReady(player1, new SpawnOfMayhem());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
        assertThat(spawn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Upkeep damage still happens if Spawn of Mayhem is destroyed in response")
    void upkeepTriggerSurvivesSourceRemoval() {
        Permanent spawn = addCreatureReady(player1, new SpawnOfMayhem());
        harness.setLife(player1, 11);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Mortify()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        advanceToUpkeep(player1);
        harness.castInstant(player2, 0, spawn.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Spawn of Mayhem");
        harness.assertNotOnBattlefield(player1, "Spawn of Mayhem");
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 19);
        assertThat(spawn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
