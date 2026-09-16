package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({NantukoDisciple.class, Forest.class, GrizzlyBears.class})
class NantukoDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+2 until end of turn when the ability resolves")
    void boostsTargetCreature() {
        setupDisciple();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void boostsOpponentsCreature() {
        setupDisciple();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Taps the disciple and spends the green mana when activated")
    void tapsOnActivation() {
        setupDisciple();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(findPermanent(player1, "Nantuko Disciple").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Spends one green mana when activated")
    void spendsGreenMana() {
        setupDisciple();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        setupDisciple();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Nantuko Disciple").isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutGreenMana() {
        setupDisciple(0);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(findPermanent(player1, "Nantuko Disciple").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        setupDisciple();
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    private void setupDisciple() {
        setupDisciple(1);
    }

    private void setupDisciple(int greenMana) {
        addCreatureReady(player1, new NantukoDisciple());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, greenMana);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
