package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AtarkaBeastbreaker;
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

@CardUsed({GladeWatcher.class, AtarkaBeastbreaker.class})
class GladeWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Defender prevents attacking without activating the ability")
    void cannotAttackWithoutActivation() {
        addWatcherReady();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Formidable ability cannot be activated below total power eight")
    void cannotActivateBelowThreshold() {
        addWatcherReady();
        addAtarkaBeastbreaker(1);
        addGreenMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power 8 or greater");
    }

    @Test
    @DisplayName("Formidable ability lets Glade Watcher attack this turn")
    void activationAllowsAttacking() {
        Permanent watcher = addWatcherReady();
        addAtarkaBeastbreaker(3);
        harness.addToBattlefield(player2, new AtarkaBeastbreaker());
        addGreenMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        assertThat(watcher.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Attack permission wears off at end of turn")
    void attackPermissionWearsOffAtEndOfTurn() {
        addWatcherReady();
        addAtarkaBeastbreaker(3);
        addGreenMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Exactly eight power including Glade Watcher permits activation")
    void canActivateAtExactlyEightPower() {
        Permanent watcher = addWatcherReady();
        addWatcherReady();
        addAtarkaBeastbreaker(1);
        addGreenMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(watcher.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Opposing creatures do not contribute to formidable")
    void opposingPowerDoesNotEnableActivation() {
        addWatcherReady();
        addAtarkaBeastbreaker(2);
        harness.addToBattlefield(player2, new GladeWatcher());
        harness.addToBattlefield(player2, new GladeWatcher());
        harness.addToBattlefield(player2, new GladeWatcher());
        addGreenMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power 8 or greater");
    }

    @Test
    @DisplayName("Formidable is not checked again when the ability resolves")
    void losingPowerBeforeResolutionDoesNotRemovePermission() {
        Permanent watcher = addWatcherReady();
        Permanent support = addWatcherReady();
        addAtarkaBeastbreaker(1);
        addGreenMana();

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(support);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(watcher.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Attack permission does not override summoning sickness")
    void summoningSickWatcherCannotAttackAfterActivation() {
        harness.addToBattlefield(player1, new GladeWatcher());
        addWatcherReady();
        addAtarkaBeastbreaker(1);
        addGreenMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private Permanent addWatcherReady() {
        return addCreatureReady(player1, new GladeWatcher());
    }

    private void addAtarkaBeastbreaker(int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player1, new AtarkaBeastbreaker());
        }
    }

    private void addGreenMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
