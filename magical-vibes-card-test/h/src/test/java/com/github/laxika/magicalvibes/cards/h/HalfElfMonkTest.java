package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HalfElfMonk.class, Forest.class})
class HalfElfMonkTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability taps the monk and paying resolves by tapping a creature")
    void tapsSourceAndTargetCreature() {
        Permanent monk = addCreatureReady(player1, new HalfElfMonk());
        Permanent target = addCreatureReady(player2, new HalfElfMonk());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(monk.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new HalfElfMonk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The ability requires both generic and white mana")
    void requiresManaCost() {
        addCreatureReady(player1, new HalfElfMonk());
        Permanent target = addCreatureReady(player2, new HalfElfMonk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The target remains untapped until the ability resolves")
    void targetIsTappedOnlyOnResolution() {
        Permanent monk = addCreatureReady(player1, new HalfElfMonk());
        Permanent target = addCreatureReady(player2, new HalfElfMonk());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(monk.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The monk can target itself even though paying the cost taps it")
    void canTargetItself() {
        Permanent monk = addCreatureReady(player1, new HalfElfMonk());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, monk.getId());
        harness.passBothPriorities();

        assertThat(monk.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped creature is a legal target")
    void canTargetTappedCreature() {
        addCreatureReady(player1, new HalfElfMonk());
        Permanent target = addCreatureReady(player2, new HalfElfMonk());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped monk cannot pay the tap cost")
    void cannotActivateWhileTapped() {
        Permanent monk = addCreatureReady(player1, new HalfElfMonk());
        Permanent target = addCreatureReady(player2, new HalfElfMonk());
        monk.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent monk = addCreatureReady(player1, new HalfElfMonk());
        Permanent target = addCreatureReady(player2, new HalfElfMonk());
        monk.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(monk.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two generic mana cannot replace the required white mana")
    void cannotPayWithoutWhiteMana() {
        Permanent monk = addCreatureReady(player1, new HalfElfMonk());
        Permanent target = addCreatureReady(player2, new HalfElfMonk());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(monk.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("One white mana does not also pay the generic mana requirement")
    void cannotPayWithOnlyOneWhiteMana() {
        Permanent monk = addCreatureReady(player1, new HalfElfMonk());
        Permanent target = addCreatureReady(player2, new HalfElfMonk());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(monk.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability still resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent monk = addCreatureReady(player1, new HalfElfMonk());
        Permanent target = addCreatureReady(player2, new HalfElfMonk());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(monk);
        gd.playerGraveyards.get(player1.getId()).add(monk.getCard());

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability can tap another creature controlled by its controller")
    void canTargetAnotherOwnCreature() {
        addCreatureReady(player1, new HalfElfMonk());
        Permanent target = addCreatureReady(player1, new HalfElfMonk());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Losing the target does not refund the activation costs")
    void targetLeavingDoesNotRefundCosts() {
        Permanent monk = addCreatureReady(player1, new HalfElfMonk());
        Permanent target = addCreatureReady(player2, new HalfElfMonk());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(monk.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }
}
