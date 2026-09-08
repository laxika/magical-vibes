package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaosLord.class, BalduvianBears.class})
class ChaosLordTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger only offers opponents as targets")
    void upkeepTriggerOnlyTargetsOpponents() {
        harness.addToBattlefield(player1, new ChaosLord());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Target opponent gains control when the number of permanents is even")
    void opponentGainsControlOnEvenPermanentCount() {
        harness.addToBattlefield(player1, new ChaosLord());
        harness.addToBattlefield(player1, new BalduvianBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chaos Lord");
        harness.assertOnBattlefield(player2, "Chaos Lord");
    }

    @Test
    @DisplayName("Parity is checked when the upkeep ability resolves")
    void parityIsCheckedOnResolution() {
        harness.addToBattlefield(player1, new ChaosLord());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new BalduvianBears());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chaos Lord");
        harness.assertOnBattlefield(player2, "Chaos Lord");
    }

    @Test
    @DisplayName("Control does not change when the number of permanents is odd")
    void controlStaysOnOddPermanentCount() {
        harness.addToBattlefield(player1, new ChaosLord());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chaos Lord");
        harness.assertNotOnBattlefield(player2, "Chaos Lord");
    }

    @Test
    @DisplayName("Counts permanents controlled by both players")
    void countsPermanentsControlledByBothPlayers() {
        harness.addToBattlefield(player1, new ChaosLord());
        harness.addToBattlefield(player2, new BalduvianBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chaos Lord");
        harness.assertOnBattlefield(player2, "Chaos Lord");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ChaosLord());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can attack while summoning sick if it did not enter the battlefield this turn")
    void attacksDespiteSummoningSickness() {
        harness.addToBattlefield(player1, new ChaosLord());
        // A blocker on the defending side so combat pauses at declare-blockers (isAttacking stays set).
        harness.addToBattlefield(player2, new BalduvianBears());

        declareAttackers(List.of(0));

        assertThat(findPermanent(player1, "Chaos Lord").isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can attack after gaining control if it entered before this turn")
    void canAttackAfterGainingControl() {
        harness.addToBattlefield(player1, new ChaosLord());
        harness.addToBattlefield(player1, new BalduvianBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));

        assertThat(findPermanent(player2, "Chaos Lord").isAttacking()).isTrue();
    }

    @Test
    @DisplayName("First strike destroys a blocking creature before it deals combat damage")
    void firstStrikeDealsDamageBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new ChaosLord());
        attacker.setAttacking(true);

        BalduvianBears blockerCard = new BalduvianBears();
        blockerCard.setPower(7);
        blockerCard.setToughness(7);
        Permanent blocker = addCreatureReady(player2, blockerCard);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Chaos Lord");
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Cannot attack on the turn it entered the battlefield")
    void cannotAttackOnTheTurnItEntered() {
        harness.setHand(player1, List.of(new ChaosLord()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
