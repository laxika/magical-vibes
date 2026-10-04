package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.e.Endurance;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LiquimetalTorque;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaeOffering.class, ChromaticStar.class, GrizzlyBears.class, DressDown.class,
        Endurance.class, LiquimetalTorque.class, OrnithopterOfParadise.class})
class FaeOfferingTest extends BaseCardTest {

    @Test
    void createsClueFoodAndTreasureAfterCastingCreatureAndNoncreatureSpells() {
        harness.addToBattlefield(player1, new FaeOffering());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ChromaticStar(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void doesNotCreateTokensUnlessBothSpellTypesWereCast() {
        harness.addToBattlefield(player1, new FaeOffering());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }

    @Test
    void doesNotTriggerWithoutCastingAnySpells() {
        harness.addToBattlefield(player1, new FaeOffering());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void doesNotTriggerAfterOnlyNoncreatureSpells() {
        harness.addToBattlefield(player1, new FaeOffering());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LiquimetalTorque()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void countsCreatureCastBeforeOfferingAndOfferingItselfAsNoncreatureSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OrnithopterOfParadise(), new FaeOffering()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Food")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void opponentsSpellsDoNotSatisfyControllersCondition() {
        harness.addToBattlefield(player1, new FaeOffering());
        castCreatureAndNoncreature(player2);

        advanceToEndStepAndResolve(player2);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Food")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void eachOfferingCreatesItsOwnSetOfTokens() {
        harness.addToBattlefield(player1, new FaeOffering());
        harness.addToBattlefield(player1, new FaeOffering());
        castCreatureAndNoncreature(player1);

        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(findPermanents(player1, "Food")).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void createdFoodCanBeSacrificedForThreeLife() {
        harness.addToBattlefield(player1, new FaeOffering());
        castCreatureAndNoncreature(player1);
        advanceToEndStepAndResolve(player1);
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int foodIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Food"));

        harness.activateAbility(player1, foodIndex, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void previousTurnsSpellsDoNotSatisfyCondition() {
        harness.addToBattlefield(player1, new FaeOffering());
        castCreatureAndNoncreature(player1);
        advanceToEndStepAndResolve(player1);
        harness.setLibrary(player2, List.of(new FaeOffering()));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        advanceToEndStepAndResolve(player2);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private void castCreatureAndNoncreature(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new LiquimetalTorque(), new OrnithopterOfParadise()));
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.castArtifact(player, 0);
        harness.passBothPriorities();
        harness.castCreature(player, 0);
        harness.passBothPriorities();
    }

    @Test
    @CardUsed({FaeOffering.class, OrnithopterOfParadise.class})
    void artifactCreatureSpellDoesNotAlsoCountAsNoncreatureSpell() {
        harness.addToBattlefield(player1, new FaeOffering());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OrnithopterOfParadise()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @CardUsed({FaeOffering.class, DressDown.class, Endurance.class})
    void triggersDuringOpponentsEndStepAfterControllerCastsBothSpellTypes() {
        harness.addToBattlefield(player1, new FaeOffering());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DressDown(), new Endurance()));
        harness.setLibrary(player1, List.of(new FaeOffering()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        advanceToEndStepAndResolve(player2);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Food")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }
}
