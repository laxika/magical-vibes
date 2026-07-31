package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BogbrewWitch;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FesteringNewtTest extends BaseCardTest {

    /**
     * Sets up combat where Festering Newt (player1) attacks and is blocked by a 3/3, so the Newt
     * dies from combat damage and its death trigger goes on the stack.
     */
    private void setupCombatWhereNewtDies() {
        Permanent newt = findPermanent(player1, "Festering Newt");
        newt.setSummoningSick(false);
        newt.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blocker = new Permanent(bigBear);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    private Permanent permanentById(UUID id) {
        return harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Death trigger gives -1/-1 without a Bogbrew Witch")
    void deathTriggerGivesMinusOne() {
        harness.addToBattlefield(player1, new FesteringNewt());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNewtDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent bears = permanentById(bearsId);
        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(bears.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Death trigger gives -4/-4 instead while controller has a Bogbrew Witch")
    void deathTriggerGivesMinusFourWithWitch() {
        harness.addToBattlefield(player1, new FesteringNewt());
        harness.addToBattlefield(player1, new BogbrewWitch());

        GrizzlyBears tough = new GrizzlyBears();
        tough.setPower(5);
        tough.setToughness(5);
        harness.addToBattlefield(player2, tough);
        UUID toughId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNewtDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, toughId);
        harness.passBothPriorities();

        Permanent target = permanentById(toughId);
        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);
    }

    @Test
    @DisplayName("A Bogbrew Witch controlled by the opponent does not upgrade the debuff")
    void opponentsWitchDoesNotUpgrade() {
        harness.addToBattlefield(player1, new FesteringNewt());
        harness.addToBattlefield(player2, new BogbrewWitch());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNewtDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent bears = permanentById(bearsId);
        assertThat(bears.getPowerModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Only creatures an opponent controls are legal targets")
    void onlyOpponentCreaturesAreLegalTargets() {
        harness.addToBattlefield(player1, new FesteringNewt());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNewtDies();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(opponentBearsId);
        assertThat(choice.validIds()).doesNotContain(ownBearsId);
    }

    @Test
    @DisplayName("Debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new FesteringNewt());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNewtDies();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(permanentById(bearsId).getPowerModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = permanentById(bearsId);
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }
}
