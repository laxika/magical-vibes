package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViashinoSlaughtermaster.class})
class ViashinoSlaughtermasterTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives it +1/+1 until end of turn")
    void abilityBoostsSelf() {
        Permanent master = addMaster(player1);
        addBg(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent master = addMaster(player1);
        addBg(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can only be activated once each turn")
    void oncePerTurn() {
        addMaster(player1);
        addBg(player1);
        addBg(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation limit applies before the first activation resolves")
    void cannotActivateAgainWhileAbilityIsOnStack() {
        Permanent master = addMaster(player1);
        addBg(player1);
        addBg(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Slaughtermaster has its own activation limit")
    void activationLimitIsPerPermanent() {
        Permanent first = addMaster(player1);
        Permanent second = addMaster(player1);
        addBg(player1);
        addBg(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability can be activated again on the opponent's turn")
    void activationLimitResetsOnNextTurn() {
        Permanent master = addMaster(player1);
        addBg(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        addBg(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped creature with summoning sickness can activate this ability")
    void tappedSummoningSickCreatureCanActivate() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new ViashinoSlaughtermaster());
        master.setSummoningSick(true);
        master.tap();
        addBg(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(2);
        assertThat(master.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Green mana alone cannot pay the activation cost")
    void activationRequiresBlackMana() {
        Permanent master = addMaster(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Black mana alone cannot pay the activation cost")
    void activationRequiresGreenMana() {
        Permanent master = addMaster(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.getEffectivePower(gd, master)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, master)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A pending activation does not boost another Slaughtermaster when its source leaves")
    void removedSourceDoesNotBoostAnotherPermanent() {
        Permanent source = addMaster(player1);
        Permanent other = addMaster(player1);
        addBg(player1);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost increases damage in both double-strike combat damage steps")
    void boostedDoubleStrikeDealsFourDamage() {
        Permanent master = addMaster(player1);
        harness.setLife(player2, 20);
        addBg(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        master.setAttacking(true);
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    private Permanent addMaster(Player player) {
        return addCreatureReady(player, new ViashinoSlaughtermaster());
    }

    private void addBg(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
