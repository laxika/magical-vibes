package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PygmyAllosaurus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurningSunCavalry.class, PygmyAllosaurus.class, GrizzlyBears.class})
class BurningSunCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 when attacking while its controller has a Dinosaur")
    void boostsOnAttackWithDinosaur() {
        Permanent cavalry = addCreatureReady(player1, new BurningSunCavalry());
        addCreatureReady(player1, new PygmyAllosaurus());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(cavalry.getPowerModifier()).isEqualTo(1);
        assertThat(cavalry.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 when blocking while its controller has a Dinosaur")
    void boostsOnBlockWithDinosaur() {
        Permanent cavalry = addCreatureReady(player1, new BurningSunCavalry());
        addCreatureReady(player1, new PygmyAllosaurus());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(cavalry.getPowerModifier()).isEqualTo(1);
        assertThat(cavalry.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a boost without a Dinosaur")
    void doesNotBoostWithoutDinosaur() {
        Permanent cavalry = addCreatureReady(player1, new BurningSunCavalry());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(cavalry.getPowerModifier()).isZero();
        assertThat(cavalry.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's Dinosaur does not enable the boost")
    void opponentDinosaurDoesNotCount() {
        Permanent cavalry = addCreatureReady(player1, new BurningSunCavalry());
        addCreatureReady(player2, new PygmyAllosaurus());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(cavalry.getPowerModifier()).isZero();
        assertThat(cavalry.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The combat boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent cavalry = addCreatureReady(player1, new BurningSunCavalry());
        addCreatureReady(player1, new PygmyAllosaurus());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cavalry.getPowerModifier()).isZero();
        assertThat(cavalry.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Attack boost resolves even after the last Dinosaur leaves")
    void attackBoostSurvivesLosingLastDinosaur() {
        Permanent cavalry = addCreatureReady(player1, new BurningSunCavalry());
        Permanent dinosaur = addCreatureReady(player1, new PygmyAllosaurus());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(dinosaur);
        gd.playerGraveyards.get(player1.getId()).add(dinosaur.getCard());
        resolveAllTriggers();

        assertThat(cavalry.getPowerModifier()).isEqualTo(1);
        assertThat(cavalry.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Block boost resolves even after the last Dinosaur leaves")
    void blockBoostSurvivesLosingLastDinosaur() {
        Permanent cavalry = addCreatureReady(player1, new BurningSunCavalry());
        Permanent dinosaur = addCreatureReady(player1, new PygmyAllosaurus());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(dinosaur);
        gd.playerGraveyards.get(player1.getId()).add(dinosaur.getCard());
        resolveAllTriggers();

        assertThat(cavalry.getPowerModifier()).isEqualTo(1);
        assertThat(cavalry.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking without a Dinosaur does not trigger a boost")
    void blockingWithoutDinosaurDoesNotTrigger() {
        Permanent cavalry = addCreatureReady(player1, new BurningSunCavalry());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(cavalry.getPowerModifier()).isZero();
        assertThat(cavalry.getToughnessModifier()).isZero();
    }
}
