package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DeadbridgeGoliath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AquusSteed.class, DeadbridgeGoliath.class})
class AquusSteedTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -2/-0 until end of turn when the ability resolves")
    void weakensTargetCreature() {
        setupSteed();
        UUID targetId = harness.getPermanentId(player2, "Deadbridge Goliath");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player2, "Deadbridge Goliath");
        assertThat(bear.getPowerModifier()).isEqualTo(-2);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Taps the Steed when activated")
    void tapsOnActivation() {
        setupSteed();
        UUID targetId = harness.getPermanentId(player2, "Deadbridge Goliath");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Aquus Steed").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Weakening wears off at cleanup")
    void weakeningWearsOff() {
        setupSteed();
        UUID targetId = harness.getPermanentId(player2, "Deadbridge Goliath");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player2, "Deadbridge Goliath");
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can target itself and reduce its power below zero without reducing toughness")
    void canTargetItself() {
        setupSteed();
        Permanent steed = findPermanent(player1, "Aquus Steed");

        harness.activateAbility(player1, 0, null, steed.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Aquus Steed");
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWhileSummoningSick() {
        setupSteed();
        Permanent steed = findPermanent(player1, "Aquus Steed");
        steed.setSummoningSick(true);
        UUID targetId = harness.getPermanentId(player2, "Deadbridge Goliath");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(steed.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Steed cannot pay the tap cost again")
    void cannotActivateWhileTapped() {
        setupSteed();
        findPermanent(player1, "Aquus Steed").tap();
        UUID targetId = harness.getPermanentId(player2, "Deadbridge Goliath");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Three generic mana cannot replace the required blue mana")
    void requiresBlueMana() {
        addCreatureReady(player1, new AquusSteed());
        harness.addToBattlefield(player2, new DeadbridgeGoliath());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player2, "Deadbridge Goliath");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "Aquus Steed").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("One blue and one generic mana are insufficient for the ability")
    void requiresFullManaCost() {
        addCreatureReady(player1, new AquusSteed());
        harness.addToBattlefield(player2, new DeadbridgeGoliath());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player2, "Deadbridge Goliath");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "Aquus Steed").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate during an opponent's turn")
    void canActivateDuringOpponentsTurn() {
        setupSteed();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.ensurePriority(player1);
        UUID targetId = harness.getPermanentId(player2, "Deadbridge Goliath");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Deadbridge Goliath").getPowerModifier()).isEqualTo(-2);
        assertThat(findPermanent(player2, "Deadbridge Goliath").getToughnessModifier()).isZero();
    }

    private void setupSteed() {
        addCreatureReady(player1, new AquusSteed());
        harness.addToBattlefield(player2, new DeadbridgeGoliath());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
