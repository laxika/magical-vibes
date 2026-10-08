package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsubstantiate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SomberwaldStag.class, GrizzlyBears.class, Unsubstantiate.class})
class SomberwaldStagTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability has Somberwald Stag fight an opponent's creature")
    void acceptingFightsOpponentCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castStag();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

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
        harness.handlePermanentChosen(player1, opponentBears.getId());
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

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentBears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Both creatures deal lethal damage in the same fight")
    void bothCreaturesCanDieInFight() {
        Permanent opponentStag = harness.addToBattlefieldAndReturn(player2, new SomberwaldStag());

        castStag();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentStag.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Somberwald Stag");
        harness.assertInGraveyard(player2, "Somberwald Stag");
        harness.assertNotOnBattlefield(player1, "Somberwald Stag");
        harness.assertNotOnBattlefield(player2, "Somberwald Stag");
    }

    @Test
    @DisplayName("A Stag that leaves before resolution does not fight")
    void sourceLeavingPreventsAllFightDamage() {
        Permanent opponentStag = harness.addToBattlefieldAndReturn(player2, new SomberwaldStag());

        castStag();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentStag.getId());
        Permanent stag = findPermanent(player1, "Somberwald Stag");
        harness.setHand(player1, List.of(new Unsubstantiate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, stag.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Somberwald Stag");
        harness.assertNotOnBattlefield(player1, "Somberwald Stag");
        harness.assertOnBattlefield(player2, "Somberwald Stag");
        assertThat(opponentStag.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves before resolution prevents the fight and the may choice")
    void targetLeavingMakesAbilityFailToResolve() {
        Permanent opponentStag = harness.addToBattlefieldAndReturn(player2, new SomberwaldStag());

        castStag();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentStag.getId());
        harness.setHand(player1, List.of(new Unsubstantiate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, opponentStag.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Somberwald Stag");
        harness.assertNotOnBattlefield(player2, "Somberwald Stag");
        assertThat(findPermanent(player1, "Somberwald Stag").getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Somberwald Stag enters normally when no legal target exists")
    void entersWithoutOpponentCreatures() {
        castStag();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Somberwald Stag");
        assertThat(findPermanent(player1, "Somberwald Stag").getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castStag() {
        harness.castFromHand(player1, new SomberwaldStag(), "{3}{G}{G}");
    }
}
