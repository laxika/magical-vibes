package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StaffOfCompleationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a permanent you own, including one an opponent controls")
    void destroysPermanentYouOwn() {
        Permanent staff = addReadyStaff();
        Card stolenCard = new GrizzlyBears();
        stolenCard.setOwnerId(player1.getId());
        Permanent stolenPermanent = harness.addToBattlefieldAndReturn(player2, stolenCard);
        gd.stolenCreatures.put(stolenPermanent.getId(), player1.getId());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, stolenPermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(stolenPermanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentPermanent);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(stolenCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(staff.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a permanent an opponent owns")
    void cannotTargetPermanentOpponentOwns() {
        addReadyStaff();
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentPermanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you own");
    }

    @Test
    @DisplayName("Pays 2 life and adds a chosen color of mana")
    void addsManaOfAnyColor() {
        Permanent staff = addReadyStaff();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(staff.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pays 3 life and proliferates")
    void proliferates() {
        addReadyStaff();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Pays 3 life and proliferates a player's poison counters")
    void proliferatesPlayerPoisonCounters() {
        addReadyStaff();
        gd.playerPoisonCounters.put(player2.getId(), 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Pays 4 life and draws a card")
    void drawsACard() {
        addReadyStaff();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(GrizzlyBears.class);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Pays 5 mana to untap the artifact")
    void untapsThisArtifact() {
        Permanent staff = addReadyStaff();
        staff.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 4, null, null);
        harness.passBothPriorities();

        assertThat(staff.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private Permanent addReadyStaff() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfCompleation());
        staff.setSummoningSick(false);
        return staff;
    }
}
