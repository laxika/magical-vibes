package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MotherOfRunes.class, GiantCockroach.class, GrimMonolith.class})
class MotherOfRunesTest extends BaseCardTest {

    @Test
    @DisplayName("The ability grants chosen-color protection to a target creature you control")
    void grantsChosenProtectionToControlledCreature() {
        addCreatureReady(player1, new MotherOfRunes());
        Permanent target = addCreatureReady(player1, new GiantCockroach());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("The granted protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new MotherOfRunes());
        Permanent target = addCreatureReady(player1, new GiantCockroach());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new MotherOfRunes());
        Permanent target = addCreatureReady(player2, new GiantCockroach());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new MotherOfRunes());
        harness.addToBattlefield(player1, new GrimMonolith());

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                0,
                0,
                null,
                harness.getPermanentId(player1, "Grim Monolith")
        )).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a creature with protection from white")
    void cannotTargetCreatureWithProtectionFromWhite() {
        Permanent firstMother = addCreatureReady(player1, new MotherOfRunes());
        Permanent target = addCreatureReady(player1, new GiantCockroach());
        Permanent secondMother = addCreatureReady(player1, new MotherOfRunes());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.WHITE.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.WHITE)).isTrue();
        assertThat(firstMother.isTapped()).isTrue();

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(secondMother.isTapped()).isFalse();
    }
}
