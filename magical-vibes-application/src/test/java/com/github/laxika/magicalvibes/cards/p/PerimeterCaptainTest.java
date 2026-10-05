package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerimeterCaptain.class, GrizzlyBears.class, WallOfWood.class})
class PerimeterCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("May gain 2 life when a creature with defender you control blocks")
    void mayGainLifeWhenControlledDefenderBlocks() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player1, new PerimeterCaptain());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("May decline to gain life from a defender blocking")
    void mayDeclineLifeGain() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player1, new PerimeterCaptain());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger when a creature without defender blocks")
    void doesNotTriggerForCreatureWithoutDefender() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new PerimeterCaptain());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature with defender blocks")
    void doesNotTriggerForOpponentsDefender() {
        addCreatureReady(player1, new PerimeterCaptain());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WallOfWood());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Another controlled defender blocking triggers the Captain")
    void triggersForAnotherControlledDefender() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player1, new PerimeterCaptain());
        addCreatureReady(player1, new WallOfWood());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each controlled defender blocking creates a separate optional life gain")
    void triggersSeparatelyForEachDefender() {
        Permanent firstAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player2, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        addCreatureReady(player1, new PerimeterCaptain());
        addCreatureReady(player1, new WallOfWood());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 22);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Captain triggers when one controlled defender blocks")
    void eachCaptainTriggersForOneBlocker() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player1, new PerimeterCaptain());
        addCreatureReady(player1, new PerimeterCaptain());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 24);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life gain still resolves after the blocking Captain leaves the battlefield")
    void triggerResolvesAfterBlockingCaptainLeaves() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent captain = addCreatureReady(player1, new PerimeterCaptain());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(captain);
        gd.playerGraveyards.get(player1.getId()).add(captain.getCard());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
    }
}
