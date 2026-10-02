package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmphinPathmage.class, RuneclawBear.class})
class AmphinPathmageTest extends BaseCardTest {

    @Test
    @DisplayName("Makes target creature unblockable until end of turn")
    void makesTargetCreatureUnblockableUntilEndOfTurn() {
        Permanent pathmage = addCreatureReady(player1, new AmphinPathmage());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(pathmage.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at cleanup")
    void unblockableWearsOffAtCleanup() {
        addCreatureReady(player1, new AmphinPathmage());
        Permanent target = addCreatureReady(player1, new RuneclawBear());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Can target itself while summoning sick and tapped")
    void canTargetItselfWhileSummoningSickAndTapped() {
        Permanent pathmage = harness.addToBattlefieldAndReturn(player1, new AmphinPathmage());
        pathmage.setSummoningSick(true);
        pathmage.tap();
        addActivationMana();

        harness.activateAbility(player1, 0, null, pathmage.getId());
        harness.passBothPriorities();

        assertThat(pathmage.isCantBeBlocked()).isTrue();
        assertThat(pathmage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate twice to make two creatures unblockable")
    void canActivateTwiceForDifferentCreatures() {
        Permanent pathmage = addCreatureReady(player1, new AmphinPathmage());
        Permanent target = addCreatureReady(player1, new RuneclawBear());
        addActivationMana();
        harness.activateAbility(player1, 0, null, pathmage.getId());
        harness.passBothPriorities();

        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(pathmage.isCantBeBlocked()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
        assertThat(pathmage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent pathmage = addCreatureReady(player1, new AmphinPathmage());
        Permanent target = addCreatureReady(player1, new RuneclawBear());
        addActivationMana();
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(pathmage);
        gd.playerGraveyards.get(player1.getId()).add(pathmage.getCard());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Affected attacker cannot be blocked")
    void affectedAttackerCannotBeBlocked() {
        addCreatureReady(player1, new AmphinPathmage());
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player2, new RuneclawBear());
        addActivationMana();
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
