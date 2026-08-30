package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackCarriage.class, BeastWalkers.class})
class BlackCarriageTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Black Carriage does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent carriage = addCreatureReady(player1, new BlackCarriage());
        carriage.tap();

        advanceToUpkeep(player1);

        assertThat(carriage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other creatures still untap normally")
    void otherPermanentsStillUntap() {
        Permanent carriage = addCreatureReady(player1, new BlackCarriage());
        carriage.tap();

        Permanent beastWalkers = addCreatureReady(player1, new BeastWalkers());
        beastWalkers.tap();

        advanceToUpkeep(player1);

        assertThat(carriage.isTapped()).isTrue();
        assertThat(beastWalkers.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a creature during your upkeep untaps Black Carriage")
    void sacrificingCreatureUntapsCarriage() {
        Permanent carriage = addCreatureReady(player1, new BlackCarriage());
        carriage.tap();
        Permanent beastWalkers = addCreatureReady(player1, new BeastWalkers());

        advanceToUpkeep(player1);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, beastWalkers.getId());
        harness.assertInGraveyard(player1, "Beast Walkers");
        harness.passBothPriorities();

        assertThat(carriage.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Beast Walkers");
    }

    @Test
    void canSacrificeBlackCarriageItselfAsCost() {
        Permanent carriage = addCreatureReady(player1, new BlackCarriage());
        carriage.tap();

        advanceToUpkeep(player1);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(carriage);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(carriage.getCard());
    }

    @Test
    @DisplayName("Cannot activate the untap ability outside your upkeep")
    void cannotActivateDuringMainPhase() {
        addCreatureReady(player1, new BlackCarriage());
        addCreatureReady(player1, new BeastWalkers());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot activate the untap ability during an opponent's upkeep")
    void cannotActivateDuringOpponentUpkeep() {
        addCreatureReady(player1, new BlackCarriage());
        addCreatureReady(player1, new BeastWalkers());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

}
