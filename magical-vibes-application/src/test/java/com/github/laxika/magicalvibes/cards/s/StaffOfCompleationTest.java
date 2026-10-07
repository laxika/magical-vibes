package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StaffOfCompleation.class, CopperLonglegs.class})
class StaffOfCompleationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a permanent you own, including one an opponent controls")
    void destroysPermanentYouOwn() {
        Permanent staff = addReadyStaff();
        Card stolenCard = new CopperLonglegs();
        stolenCard.setOwnerId(player1.getId());
        Permanent stolenPermanent = harness.addToBattlefieldAndReturn(player2, stolenCard);
        gd.stolenCreatures.put(stolenPermanent.getId(), player1.getId());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
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
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());

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
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Pays 3 life and proliferates")
    void proliferates() {
        addReadyStaff();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
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
        harness.setLibrary(player1, List.of(new CopperLonglegs()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(CopperLonglegs.class);
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

    @Test
    @DisplayName("Can destroy itself immediately after entering the battlefield")
    void destroysItselfWithoutWaitingATurn() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfCompleation());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, staff.getId());

        assertThat(staff.isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(staff);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(staff);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(staff.getCard());
    }

    @Test
    @DisplayName("Cannot pay a life cost greater than the current life total")
    void cannotActivateWithInsufficientLife() {
        Permanent staff = addReadyStaff();
        harness.setLife(player1, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        harness.assertLife(player1, 3);
        assertThat(staff.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May choose no permanents or players when proliferating")
    void mayDeclineAllProliferationChoices() {
        Permanent staff = addReadyStaff();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        harness.assertLife(player1, lifeBefore - 3);
        assertThat(staff.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Proliferates every existing counter kind on chosen permanents and players")
    void proliferatesMultipleKindsAndBothControllers() {
        Permanent staff = addReadyStaff();
        staff.setCounterCount(CounterType.CHARGE, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.CHARGE, 3);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 4);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(staff.getId(), creature.getId(), player2.getId()));

        assertThat(staff.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("Untapping allows another activation in the same turn")
    void canActivateAgainAfterUntapping() {
        Permanent staff = addReadyStaff();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new CopperLonglegs(), new CopperLonglegs()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 4, null, null);
        assertThat(staff.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(staff.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, lifeBefore - 8);
        assertThat(staff.isTapped()).isTrue();
    }

    private Permanent addReadyStaff() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfCompleation());
        staff.setSummoningSick(false);
        return staff;
    }
}
