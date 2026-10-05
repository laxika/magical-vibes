package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Dominate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.v.Vendetta;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Insubordination.class, FreshVolunteers.class, Forest.class, Vendetta.class, Dominate.class})
class InsubordinationTest extends BaseCardTest {

    private Permanent attachToOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new FreshVolunteers());

        harness.setHand(player1, List.of(new Insubordination()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        return creature;
    }

    private void runEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.END_STEP);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Deals 2 damage to the enchanted creature's controller at their end step")
    void damagesEnchantedCreatureController() {
        attachToOpponentCreature();

        int player1Life = gd.playerLifeTotals.get(player1.getId());
        int player2Life = gd.playerLifeTotals.get(player2.getId());

        runEndStep(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1Life);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2Life - 2);
    }

    @Test
    @DisplayName("Does not deal damage if the enchanted creature attacked this turn")
    void doesNotDamageWhenEnchantedCreatureAttacked() {
        Permanent creature = attachToOpponentCreature();
        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(creature)));
        resolveCombat(player2);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        runEndStep(player2);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Triggers only during the enchanted creature controller's end step")
    void doesNotTriggerOnAurasControllerEndStep() {
        attachToOpponentCreature();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        runEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Checks the unless condition when the trigger resolves")
    void checksUnlessConditionAtResolution() {
        Permanent creature = attachToOpponentCreature();

        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        creature.setAttackedThisTurn(true);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Still damages the original controller when the nonattacking creature dies in response")
    void damagesControllerAfterCreatureDies() {
        Permanent creature = attachToOpponentCreature();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(player2, TurnStep.END_STEP);
            assertThat(gd.stack).hasSize(1);
            harness.castInstant(player1, 0, creature.getId());
            resolveAllTriggers();
        });

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Fresh Volunteers");
        harness.assertInGraveyard(player1, "Insubordination");
    }

    @Test
    @DisplayName("Damages the player whose end step triggered it after the creature changes controllers")
    void damagesOriginalControllerAfterControlChanges() {
        Permanent creature = attachToOpponentCreature();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new Dominate()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(player2, TurnStep.END_STEP);
            assertThat(gd.stack).hasSize(1);
            harness.castInstant(player1, 0, 2, creature.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Insubordination()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent forest = findPermanent(player2, "Forest");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
