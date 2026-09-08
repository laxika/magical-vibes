package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({Earthlore.class, Forest.class, BalduvianBears.class})
class EarthloreTest extends BaseCardTest {

    private Permanent land;
    private Permanent blocker;

    private void setupEarthloreOnLand() {
        land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Earthlore());
        aura.setAttachedTo(land.getId());

        blocker = addCreatureReady(player1, new BalduvianBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
    }

    @Test
    @DisplayName("Can enchant a land you control")
    void enchantsLandYouControl() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Earthlore()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Earthlore");
        assertThat(aura.getAttachedTo()).isEqualTo(land.getId());
    }

    @Test
    @DisplayName("Cannot enchant a land an opponent controls")
    void rejectsLandOpponentControls() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Earthlore()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land you control");
    }

    @Test
    @DisplayName("Tapping the enchanted land gives target blocking creature +1/+2")
    void boostsBlockingCreature() {
        setupEarthloreOnLand();

        harness.activateAbility(player1, 1, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        setupEarthloreOnLand();

        harness.activateAbility(player1, 1, null, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a blocking creature an opponent controls")
    void boostsOpposingBlockingCreature() {
        setupEarthloreOnLand();
        Permanent opposingBlocker = addCreatureReady(player2, new BalduvianBears());
        opposingBlocker.setBlocking(true);
        opposingBlocker.addBlockingTarget(0);

        harness.activateAbility(player1, 1, null, opposingBlocker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingBlocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingBlocker)).isEqualTo(4);
    }

    @Test
    @DisplayName("A target that stops blocking before resolution is illegal")
    void rejectsTargetThatStopsBlocking() {
        setupEarthloreOnLand();

        harness.activateAbility(player1, 1, null, blocker.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate while the enchanted land is tapped")
    void cannotActivateWithTappedLand() {
        setupEarthloreOnLand();
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while the Aura is not attached")
    void cannotActivateWhenUnattached() {
        harness.addToBattlefield(player1, new Earthlore());
        blocker = addCreatureReady(player1, new BalduvianBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature that is not blocking is an illegal target")
    void rejectsNonBlockingCreature() {
        setupEarthloreOnLand();
        Permanent idle = addCreatureReady(player1, new BalduvianBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, idle.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
    }
}
