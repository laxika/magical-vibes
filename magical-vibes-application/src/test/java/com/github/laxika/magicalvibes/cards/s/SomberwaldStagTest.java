package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SomberwaldStagTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability has Somberwald Stag fight an opponent's creature")
    void acceptingFightsOpponentCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castStag();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, opponentBears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        Permanent stag = findPermanent(player1, "Somberwald Stag");
        assertThat(stag.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the ETB ability does not cause a fight")
    void decliningDoesNotFight() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castStag();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(opponentBears.getMarkedDamage()).isZero();
        assertThat(findPermanent(player1, "Somberwald Stag").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The ETB ability cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castStag();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentBears.getMarkedDamage()).isZero();
    }

    private void castStag() {
        harness.setHand(player1, List.of(new SomberwaldStag()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
