package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.v.VedalkenOrrery;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeizeTheDay.class, WoodlandDruid.class, Plains.class, VedalkenOrrery.class})
class SeizeTheDayTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps the target creature and creates an additional combat and main phase")
    void untapsTargetCreatureAndCreatesAdditionalPhases() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        creature.tap();

        castFromPostcombatMain(creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();

        GameData gd = harness.getGameData();
        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("From precombat main, returns to the regular combat after the added main phase")
    void precombatCastReturnsToRegularCombatAfterAdditionalMainPhase() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        creature.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SeizeTheDay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Can target and untap a creature controlled by an opponent")
    void canTargetAndUntapOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WoodlandDruid());
        creature.tap();

        castFromPostcombatMain(creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only a creature can be targeted")
    void onlyCreatureCanBeTargeted() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SeizeTheDay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback resolves the spell and exiles it")
    void flashbackResolvesAndExilesSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        creature.tap();
        harness.setGraveyard(player1, List.of(new SeizeTheDay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        assertThat(creature.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Seize the Day");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Seize the Day"));
    }

    @Test
    @DisplayName("An already untapped creature is a legal target and still adds phases")
    void untappedTargetStillAddsPhases() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());

        castFromPostcombatMain(creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Casting and flashing back in the same main phase adds two combat and main pairs")
    void normalCastAndFlashbackAddTwoPhasePairs() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        castFromPostcombatMain(creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Seize the Day");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        for (int i = 0; i < 2; i++) {
            gs.advanceStep(gd);
            assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
            gs.advanceStep(gd);
            assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
            gs.advanceStep(gd);
            assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
            gs.advanceStep(gd);
            assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        }
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Untapping is limited to the targeted creature")
    void leavesOtherCreaturesTapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        target.tap();
        other.tap();

        castFromPostcombatMain(target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An illegal target prevents both additional phases")
    void removedTargetPreventsAdditionalPhases() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        castFromPostcombatMain(creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Seize the Day");
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Flashback with an illegal target is exiled and adds no phases")
    void illegalFlashbackTargetStillExilesSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SeizeTheDay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFlashback(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Seize the Day");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Seize the Day"));
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Resolving during combat untaps the target without adding phases")
    void resolvingOutsideMainPhaseDoesNotAddPhases() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        creature.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SeizeTheDay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(creature.isTapped()).isFalse();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Casting during the opponent's main phase adds phases to that opponent's turn")
    void opponentsMainPhaseAddsPhasesForOpponent() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WoodlandDruid());
        creature.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SeizeTheDay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(creature.isTapped()).isFalse();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        gs.advanceStep(gd);
        gs.advanceStep(gd);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }

    private void castFromPostcombatMain(java.util.UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SeizeTheDay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, targetId);
    }
}
