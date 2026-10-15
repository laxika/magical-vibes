package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmolatingSouleater.class})
class ImmolatingSouleaterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        Permanent perm = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("Resolving ability gives +1/+0 until end of turn")
    void resolvingGivesPlusOnePlusZero() {
        Permanent perm = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(1);
        assertThat(perm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate multiple times to stack the bonus")
    void canActivateMultipleTimes() {
        Permanent perm = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(3);
        assertThat(perm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Bonus resets at end of turn cleanup")
    void bonusResetsAtEndOfTurn() {
        Permanent perm = addSouleaterReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(0);
        assertThat(perm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can pay Phyrexian mana with life instead of red mana")
    void canPayPhyrexianWithLife() {
        Permanent perm = addSouleaterReady(player1);
        harness.setLife(player1, 20);
        // No red mana — will pay 2 life for {R/P}

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(perm.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot pay two life when only one life remains")
    void cannotPayWithInsufficientLife() {
        Permanent perm = addSouleaterReady(player1);
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        assertThat(gd.stack).isEmpty();
        assertThat(perm.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Red mana can pay the cost even with only one life remaining")
    void canPayWithRedManaAtOneLife() {
        Permanent perm = addSouleaterReady(player1);
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        assertThat(perm.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new ImmolatingSouleater());
        perm.setSummoningSick(true);
        perm.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost affects only the source Souleater and waits for resolution")
    void boostsOnlySourceOnResolution() {
        Permanent source = addSouleaterReady(player1);
        Permanent ally = addSouleaterReady(player1);
        Permanent opponent = addSouleaterReady(player2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 18);
        assertThat(source.getPowerModifier()).isZero();

        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isZero();
        assertThat(ally.getPowerModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
    }

    private Permanent addSouleaterReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ImmolatingSouleater());
        perm.setSummoningSick(false);
        return perm;
    }
}
