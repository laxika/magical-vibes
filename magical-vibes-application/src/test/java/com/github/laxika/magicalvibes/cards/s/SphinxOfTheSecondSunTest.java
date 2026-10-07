package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AggravatedAssault;
import com.github.laxika.magicalvibes.cards.d.DarksteelMutation;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphinxOfTheSecondSun.class, GrizzlyBears.class, PhyrexianArena.class, AggravatedAssault.class, DarksteelMutation.class})
class SphinxOfTheSecondSunTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an additional beginning phase after postcombat main")
    void createsAdditionalBeginningPhaseAfterPostcombatMain() {
        harness.addToBattlefield(player1, new SphinxOfTheSecondSun());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        Card draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));
        gd.turnNumber = 2;

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.stack).hasSize(1);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(tappedCreature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("Each Sphinx creates a separate complete beginning phase")
    void multipleSphinxesCreateMultipleBeginningPhases() {
        harness.addToBattlefield(player1, new SphinxOfTheSecondSun());
        harness.addToBattlefield(player1, new SphinxOfTheSecondSun());
        Card firstDraw = new SphinxOfTheSecondSun();
        Card secondDraw = new SphinxOfTheSecondSun();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        gd.turnNumber = 2;

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.stack).hasSize(2);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(2);
    }

    @Test
    @DisplayName("The added upkeep triggers upkeep abilities before the added draw step")
    void additionalBeginningPhaseTriggersUpkeepAbilities() {
        harness.addToBattlefield(player1, new SphinxOfTheSecondSun());
        harness.addToBattlefield(player1, new PhyrexianArena());
        Card upkeepDraw = new SphinxOfTheSecondSun();
        Card turnBasedDraw = new SphinxOfTheSecondSun();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(upkeepDraw, turnBasedDraw));
        harness.setLife(player1, 20);
        gd.turnNumber = 2;

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(upkeepDraw, turnBasedDraw);
        harness.assertLife(player1, 19);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving the trigger does not end the current main phase")
    void resolvingTriggerLeavesPostcombatMainInProgress() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfTheSecondSun());
        sphinx.tap();
        Card draw = new SphinxOfTheSecondSun();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw));
        gd.turnNumber = 2;

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(sphinx.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(sphinx.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's postcombat main phase")
    void doesNotTriggerDuringOpponentsPostcombatMain() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfTheSecondSun());
        sphinx.tap();
        Card draw = new SphinxOfTheSecondSun();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(draw));
        gd.turnNumber = 2;

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(sphinx.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.turnNumber).isEqualTo(2);
    }

    @Test
    @DisplayName("The extra untap does not cure summoning sickness during the same turn")
    void extraUntapDoesNotRemoveSummoningSickness() {
        harness.addToBattlefield(player1, new SphinxOfTheSecondSun());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new SphinxOfTheSecondSun()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gd.turnNumber = 2;

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
        });
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isSummoningSick()).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.turnNumber).isEqualTo(2);
        assertThat(bears.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("A new beginning phase precedes an older queued extra combat")
    void beginningPhasePrecedesOlderQueuedCombat() {
        harness.addToBattlefield(player1, new SphinxOfTheSecondSun());
        harness.addToBattlefield(player1, new AggravatedAssault());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new SphinxOfTheSecondSun(), new SphinxOfTheSecondSun(), new SphinxOfTheSecondSun()));
        gd.turnNumber = 2;

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, harness::passBothPriorities);
        harness.addMana(player1, ManaColor.RED, 10);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 1, null, null);
            harness.passBothPriorities();
            harness.activateAbility(player1, 1, null, null);
            harness.passBothPriorities();
        });

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.turnNumber).isEqualTo(2);
    }

    @Test
    @DisplayName("A Sphinx that has lost its abilities does not trigger")
    void sphinxWithRemovedAbilitiesDoesNotTrigger() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfTheSecondSun());
        harness.setHand(player1, List.of(new DarksteelMutation()));
        harness.setLibrary(player1, List.of(new SphinxOfTheSecondSun()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        gd.turnNumber = 2;

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castEnchantment(player1, 0, sphinx.getId());
            harness.passBothPriorities();
        });
        assertThat(gqs.hasLostAllAbilities(gd, sphinx)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.turnNumber).isEqualTo(2);
    }
}
