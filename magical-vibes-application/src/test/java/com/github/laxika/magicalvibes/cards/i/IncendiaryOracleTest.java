package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.t.TranscendentEnvoy;
import com.github.laxika.magicalvibes.cards.d.DragToTheUnderworld;
import com.github.laxika.magicalvibes.cards.r.RiptideTurtle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IncendiaryOracle.class, TranscendentEnvoy.class, RiptideTurtle.class,
        DragToTheUnderworld.class, Ichthyomorphosis.class})
class IncendiaryOracleTest extends BaseCardTest {

    @Test
    void activatedAbilityBoostsPowerUntilEndOfTurn() {
        Permanent oracle = addCreatureReady(player1, new IncendiaryOracle());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(oracle.getEffectivePower()).isEqualTo(3);
        assertThat(oracle.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(oracle.getEffectivePower()).isEqualTo(2);
        assertThat(oracle.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void creatureDamagedByIncendiaryOracleIsExiledInsteadOfDying() {
        Permanent oracle = addCreatureReady(player1, new IncendiaryOracle());
        oracle.setAttacking(true);
        addCreatureReady(player2, new TranscendentEnvoy());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Transcendent Envoy");
        harness.assertNotInGraveyard(player2, "Transcendent Envoy");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Transcendent Envoy"));
    }

    @Test
    void bothOraclesAreExiledWhenTheyDealLethalDamageSimultaneously() {
        addCreatureReady(player1, new IncendiaryOracle()).setAttacking(true);
        addCreatureReady(player2, new IncendiaryOracle());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Incendiary Oracle");
        harness.assertNotOnBattlefield(player2, "Incendiary Oracle");
        harness.assertNotInGraveyard(player1, "Incendiary Oracle");
        harness.assertNotInGraveyard(player2, "Incendiary Oracle");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Incendiary Oracle"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Incendiary Oracle"));
    }

    @Test
    void survivingCreatureIsExiledIfDestroyedLaterInTheTurn() {
        Permanent turtle = damageTurtleInCombat();
        destroyCreature(turtle);

        harness.assertNotOnBattlefield(player2, "Riptide Turtle");
        harness.assertNotInGraveyard(player2, "Riptide Turtle");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Riptide Turtle"));
    }

    @Test
    void replacementStopsApplyingAfterOracleLeavesTheBattlefield() {
        Permanent turtle = damageTurtleInCombat();
        destroyCreature(findPermanent(player1, "Incendiary Oracle"));
        destroyCreature(turtle);

        harness.assertInGraveyard(player1, "Incendiary Oracle");
        harness.assertInGraveyard(player2, "Riptide Turtle");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void replacementStopsApplyingAfterOracleLosesItsAbilities() {
        Permanent turtle = damageTurtleInCombat();
        Permanent oracle = findPermanent(player1, "Incendiary Oracle");
        harness.setHand(player1, List.of(new Ichthyomorphosis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castEnchantment(player1, 0, oracle.getId());
        harness.passBothPriorities();
        destroyCreature(turtle);

        harness.assertInGraveyard(player2, "Riptide Turtle");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private Permanent damageTurtleInCombat() {
        addCreatureReady(player1, new IncendiaryOracle()).setAttacking(true);
        Permanent turtle = addCreatureReady(player2, new RiptideTurtle());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Incendiary Oracle");
        harness.assertOnBattlefield(player2, "Riptide Turtle");
        assertThat(turtle.getMarkedDamage()).isEqualTo(2);
        return turtle;
    }

    private void destroyCreature(Permanent creature) {
        harness.setHand(player1, List.of(new DragToTheUnderworld()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
    }
}
