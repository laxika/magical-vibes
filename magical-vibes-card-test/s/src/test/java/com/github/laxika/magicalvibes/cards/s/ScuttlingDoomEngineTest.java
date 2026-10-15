package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.i.InvasiveSpecies;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.f.FleshToDust;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScuttlingDoomEngine.class, RuneclawBear.class, InvasiveSpecies.class, LilianaVess.class, FleshToDust.class})
class ScuttlingDoomEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Scuttling Doom Engine can't be blocked by a creature with power 2")
    void cannotBeBlockedByPowerTwoCreature() {
        Permanent blockerPerm = setUpCombat(new RuneclawBear());

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Scuttling Doom Engine can be blocked by a creature with power 3")
    void canBeBlockedByPowerThreeCreature() {
        Permanent blockerPerm = setUpCombat(new InvasiveSpecies());

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, 0)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The death trigger deals 6 damage to the chosen opponent")
    void deathTriggerDealsSixToOpponent() {
        harness.addToBattlefield(player1, new ScuttlingDoomEngine());
        killDoomEngine();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("The controller is not a legal target for the death trigger")
    void controllerIsNotALegalTarget() {
        harness.addToBattlefield(player1, new ScuttlingDoomEngine());
        killDoomEngine();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2.getId())
                .doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("The death trigger can hit an opponent's planeswalker instead of a creature")
    void deathTriggerCanHitPlaneswalker() {
        harness.addToBattlefield(player1, new ScuttlingDoomEngine());
        harness.addToBattlefield(player2, new LilianaVess());
        harness.addToBattlefield(player2, new RuneclawBear());
        Permanent liliana = findPermanent(player2, "Liliana Vess");
        liliana.setCounterCount(CounterType.LOYALTY, 5);
        UUID lilianaId = liliana.getId();
        UUID bearsId = harness.getPermanentId(player2, "Runeclaw Bear");

        killDoomEngine();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(lilianaId)
                .doesNotContain(bearsId);

        harness.handlePermanentChosen(player1, lilianaId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Liliana Vess"));
    }

    @Test
    @DisplayName("A creature raised from power 2 to 3 can block")
    void canBeBlockedAfterPowerIncrease() {
        Permanent blocker = setUpCombat(new RuneclawBear());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature reduced from power 3 to 2 cannot block")
    void cannotBeBlockedAfterPowerDecrease() {
        Permanent blocker = setUpCombat(new InvasiveSpecies());
        blocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The death trigger can deal exactly 6 damage to its controller's planeswalker")
    void deathTriggerCanHitOwnPlaneswalker() {
        harness.addToBattlefield(player1, new ScuttlingDoomEngine());
        Permanent liliana = harness.addToBattlefieldAndReturn(player1, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 8);

        killDoomEngine();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(liliana.getId())
                .doesNotContain(player1.getId());
        harness.handlePermanentChosen(player1, liliana.getId());
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(liliana);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    /** Player2 destroys the Doom Engine on their own turn. */
    private void killDoomEngine() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FleshToDust()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        UUID engineId = harness.getPermanentId(player1, "Scuttling Doom Engine");
        harness.castAndResolveInstant(player2, 0, engineId);
    }

    /** Attacks with the Doom Engine and puts the given creature on player2's side as a potential blocker. */
    private Permanent setUpCombat(com.github.laxika.magicalvibes.model.Card blocker) {
        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new ScuttlingDoomEngine());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, blocker);
        blockerPerm.setSummoningSick(false);

        prepareDeclareBlockers(player1);

        return blockerPerm;
    }
}
