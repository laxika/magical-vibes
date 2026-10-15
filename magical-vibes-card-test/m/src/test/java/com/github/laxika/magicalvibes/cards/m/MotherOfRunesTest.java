package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.cards.s.Snap;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MotherOfRunes.class, GiantCockroach.class, GrimMonolith.class, Snap.class})
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
        harness.passUntil(player2, TurnStep.UPKEEP);

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

    @ParameterizedTest
    @EnumSource(CardColor.class)
    void canProtectItselfFromEachColor(CardColor color) {
        Permanent mother = addCreatureReady(player1, new MotherOfRunes());

        harness.activateAbility(player1, 0, 0, null, mother.getId());

        assertThat(mother.isTapped()).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, mother, color)).isFalse();

        harness.passBothPriorities();
        harness.handleListChoice(player1, color.name());

        assertThat(gqs.hasProtectionFrom(gd, mother, color)).isTrue();
    }

    @Test
    void summoningSicknessPreventsActivation() {
        Permanent mother = harness.addToBattlefieldAndReturn(player1, new MotherOfRunes());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, mother.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mother.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileAlreadyTapped() {
        Permanent mother = addCreatureReady(player1, new MotherOfRunes());
        mother.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, mother.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void gainingProtectionFromWhiteInResponseMakesEarlierAbilityIllegal() {
        addCreatureReady(player1, new MotherOfRunes());
        Permanent target = addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new MotherOfRunes());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player1, 2, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.WHITE.name());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.WHITE)).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void abilityStillResolvesAfterMotherLeavesBattlefield() {
        Permanent mother = addCreatureReady(player1, new MotherOfRunes());
        Permanent target = addCreatureReady(player1, new GiantCockroach());
        harness.setHand(player2, List.of(new Snap()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.castInstant(player2, 0, mother.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Mother of Runes");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.GREEN.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.GREEN)).isTrue();
    }

    @Test
    void abilityDoesNotResolveWhenTargetLeavesBattlefield() {
        Permanent mother = addCreatureReady(player1, new MotherOfRunes());
        Permanent target = addCreatureReady(player1, new GiantCockroach());
        harness.setHand(player2, List.of(new Snap()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Giant Cockroach");
        harness.passBothPriorities();

        assertThat(mother.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void protectionFromBlueStopsAnAlreadyCastBlueSpell() {
        addCreatureReady(player1, new MotherOfRunes());
        Permanent target = addCreatureReady(player1, new GiantCockroach());
        harness.setHand(player2, List.of(new Snap()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, target.getId());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.BLUE.name());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Cockroach");
        harness.assertInGraveyard(player2, "Snap");
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void separateActivationsCanGrantProtectionFromMultipleColors() {
        addCreatureReady(player1, new MotherOfRunes());
        Permanent target = addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new MotherOfRunes());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player1, 2, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.BLUE.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.WHITE)).isFalse();
    }
}
