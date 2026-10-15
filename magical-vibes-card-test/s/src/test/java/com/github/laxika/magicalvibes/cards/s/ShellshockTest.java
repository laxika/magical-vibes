package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OraclesAttendants;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({Shellshock.class, GrizzlyBears.class, OraclesAttendants.class})
class ShellshockTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to the chosen creature and creates one Mutagen")
    void damagesChosenCreatureAndCreatesMutagen() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castShellshock(1, List.of(bear.getId()));

        assertThat(bear.getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Creates no Mutagen when X is zero")
    void createsNoMutagenWhenNoDamageIsDealt() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castShellshock(0, List.of(bear.getId()));

        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    @DisplayName("Allows choosing no targets")
    void allowsChoosingNoTargets() {
        castShellshock(1, List.of());

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    @DisplayName("Allows at most one creature controlled by an opponent")
    void allowsAtMostOneCreaturePerOpponent() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareShellshock(1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1,
                List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareShellshock(1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, ownBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lethalDamageStillCreatesOneMutagen() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castShellshock(3, List.of(bear.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void createdTokenHasMutagenSubtype() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castShellshock(1, List.of(bear.getId()));

        assertThat(findPermanent(player1, "Mutagen").getCard().getSubtypes())
                .contains(CardSubtype.MUTAGEN);
    }

    @Test
    void fullyPreventedDamageCreatesNoMutagen() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bear.setDamagePreventionShield(2);

        castShellshock(2, List.of(bear.getId()));

        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    void partiallyPreventedDamageStillCreatesOneMutagen() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bear.setDamagePreventionShield(1);

        castShellshock(2, List.of(bear.getId()));

        assertThat(bear.getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void targetBecomingControlledByCasterCreatesNoMutagen() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareShellshock(1);
        harness.castInstantForX(player1, 0, 1, List.of(bear.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(bear);
        gd.playerBattlefields.get(player1.getId()).add(bear);

        resolveAllTriggers();

        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        harness.assertInGraveyard(player1, "Shellshock");
    }

    @Test
    void mutagenSacrificeIsPaidBeforeCounterResolvesAndCanTargetOpponentCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castShellshock(1, List.of(bear.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bear.getId());

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mutagenCannotActivateOutsideMainPhase() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castShellshock(1, List.of(bear.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void damageRedirectedToAnotherCreatureStillCreatesMutagen() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent attendants = addCreatureReady(player2, new OraclesAttendants());
        prepareShellshock(1);
        java.util.UUID spellId = gd.playerHands.get(player1.getId()).getFirst().getId();
        harness.castInstantForX(player1, 0, 1, List.of(bear.getId()));
        harness.activateAbility(player2, 1, null, bear.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, spellId);

        resolveAllTriggers();

        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(attendants.getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    private void castShellshock(int xValue, List<java.util.UUID> targetIds) {
        prepareShellshock(xValue);
        harness.castInstantForX(player1, 0, xValue, targetIds);
        resolveAllTriggers();
    }

    private void prepareShellshock(int xValue) {
        harness.setHand(player1, List.of(new Shellshock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
