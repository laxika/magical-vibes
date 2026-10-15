package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AzoriusLocket;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TenthDistrictVeteran.class, TerritorialBoar.class, AzoriusLocket.class})
class TenthDistrictVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking untaps another tapped creature you control")
    void attackUntapsAnotherCreatureYouControl() {
        Permanent veteran = addCreatureReady(player1, new TenthDistrictVeteran());
        Permanent boar = addCreatureReady(player1, new TerritorialBoar());
        boar.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, boar.getId());
        harness.passBothPriorities();

        assertThat(boar.isTapped()).isFalse();
        assertThat(veteran.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attack trigger cannot target the attacking Veteran")
    void cannotTargetItself() {
        Permanent veteran = addCreatureReady(player1, new TenthDistrictVeteran());
        Permanent otherVeteran = addCreatureReady(player1, new TenthDistrictVeteran());
        otherVeteran.tap();

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, veteran.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, otherVeteran.getId());
        harness.passBothPriorities();

        assertThat(otherVeteran.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attack trigger cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new TenthDistrictVeteran());
        Permanent ownCreature = addCreatureReady(player1, new TerritorialBoar());
        Permanent opponentCreature = addCreatureReady(player2, new TerritorialBoar());
        ownCreature.tap();
        opponentCreature.tap();

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attack trigger cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new TenthDistrictVeteran());
        Permanent boar = addCreatureReady(player1, new TerritorialBoar());
        Permanent locket = harness.addToBattlefieldAndReturn(player1, new AzoriusLocket());
        boar.tap();
        locket.tap();

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, locket.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, boar.getId());
        harness.passBothPriorities();

        assertThat(boar.isTapped()).isFalse();
        assertThat(locket.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An untapped creature is a legal target")
    void canTargetUntappedCreature() {
        addCreatureReady(player1, new TenthDistrictVeteran());
        Permanent boar = addCreatureReady(player1, new TerritorialBoar());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, boar.getId());
        boar.tap();
        harness.passBothPriorities();

        assertThat(boar.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attacking alone does not leave an unanswerable target prompt")
    void attacksWithoutLegalTargets() {
        Permanent veteran = addCreatureReady(player1, new TenthDistrictVeteran());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(veteran.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attack trigger still resolves after Veteran leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent veteran = addCreatureReady(player1, new TenthDistrictVeteran());
        Permanent boar = addCreatureReady(player1, new TerritorialBoar());
        boar.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, boar.getId());
        gd.playerBattlefields.get(player1.getId()).remove(veteran);
        gd.playerGraveyards.get(player1.getId()).add(veteran.getCard());
        harness.passBothPriorities();

        assertThat(boar.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature that changes to the opponent's control is not untapped")
    void doesNotUntapTargetNoLongerControlled() {
        addCreatureReady(player1, new TenthDistrictVeteran());
        Permanent boar = addCreatureReady(player1, new TerritorialBoar());
        boar.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, boar.getId());
        gd.playerBattlefields.get(player1.getId()).remove(boar);
        gd.playerBattlefields.get(player2.getId()).add(boar);
        harness.passBothPriorities();

        assertThat(boar.isTapped()).isTrue();
    }
}
