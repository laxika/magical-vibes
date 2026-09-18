package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Shellshock.class, GrizzlyBears.class})
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

    private void castShellshock(int xValue, List<java.util.UUID> targetIds) {
        prepareShellshock(xValue);
        harness.castInstantForX(player1, 0, xValue, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareShellshock(int xValue) {
        harness.setHand(player1, List.of(new Shellshock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
