package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrostbridgeGuard.class, Forest.class})
class FrostbridgeGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability taps target creature")
    void resolvingTapsTargetCreature() {
        addCreatureReady(player1, new FrostbridgeGuard());
        Permanent target = addCreatureReady(player2, new FrostbridgeGuard());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability taps the guard and pays its mana cost")
    void activatingPaysCostAndTapsGuard() {
        Permanent guard = addCreatureReady(player1, new FrostbridgeGuard());
        Permanent target = addCreatureReady(player2, new FrostbridgeGuard());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(guard.isTapped()).isTrue();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Can target a creature controlled by its controller")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new FrostbridgeGuard());
        Permanent target = addCreatureReady(player1, new FrostbridgeGuard());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new FrostbridgeGuard());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new FrostbridgeGuard());
        guard.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new FrostbridgeGuard());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(guard.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileAlreadyTapped() {
        Permanent guard = addCreatureReady(player1, new FrostbridgeGuard());
        guard.tap();
        Permanent target = addCreatureReady(player2, new FrostbridgeGuard());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canTargetAnAlreadyTappedCreature() {
        Permanent guard = addCreatureReady(player1, new FrostbridgeGuard());
        Permanent target = addCreatureReady(player2, new FrostbridgeGuard());
        target.tap();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(guard.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetItself() {
        Permanent guard = addCreatureReady(player1, new FrostbridgeGuard());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, guard.getId());
        harness.passBothPriorities();

        assertThat(guard.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent guard = addCreatureReady(player1, new FrostbridgeGuard());
        Permanent target = addCreatureReady(player2, new FrostbridgeGuard());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(guard);
        gd.playerGraveyards.get(player1.getId()).add(guard.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }
}
