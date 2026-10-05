package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BorealCentaur;
import com.github.laxika.magicalvibes.cards.s.SimianBrawler;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OhranViper.class, BorealCentaur.class, SimianBrawler.class, SnowCoveredForest.class})
class OhranViperTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature destroys it at end of combat")
    void combatDamageDestroysCreatureAtEndOfCombat() {
        Permanent viper = addCreatureReady(player1, new OhranViper());
        viper.setAttacking(true);
        addCreatureReady(player2, new BorealCentaur());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player2, "Boreal Centaur");
        harness.assertOnBattlefield(player1, "Ohran Viper");
    }

    @Test
    @DisplayName("Combat damage does not destroy the creature immediately")
    void combatDamageDoesNotDestroyCreatureImmediately() {
        Permanent viper = addCreatureReady(player1, new OhranViper());
        viper.setAttacking(true);
        addCreatureReady(player2, new BorealCentaur());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertOnBattlefield(player1, "Ohran Viper");
        harness.assertOnBattlefield(player2, "Boreal Centaur");

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player2, "Boreal Centaur");
    }

    @Test
    @DisplayName("The destruction trigger still resolves if Ohran Viper dies in combat")
    void destructionTriggerStillResolvesIfViperDiesInCombat() {
        Permanent viper = addCreatureReady(player1, new OhranViper());
        viper.setAttacking(true);
        addCreatureReady(player2, new SimianBrawler());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertNotOnBattlefield(player1, "Ohran Viper");
        harness.assertOnBattlefield(player2, "Simian Brawler");
        assertThat(gd.stack).anyMatch(stackEntry ->
                stackEntry.getCard().getName().equals("Ohran Viper"));

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player2, "Simian Brawler");
    }

    @Test
    @DisplayName("Combat damage to a player may draw a card")
    void combatDamageToPlayerMayDraw() {
        Permanent viper = addCreatureReady(player1, new OhranViper());
        viper.setAttacking(true);
        harness.setLibrary(player1, new ArrayList<>(List.of(new SnowCoveredForest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the card draw draws nothing")
    void decliningCardDrawDrawsNothing() {
        Permanent viper = addCreatureReady(player1, new OhranViper());
        viper.setAttacking(true);
        harness.setLibrary(player1, new ArrayList<>(List.of(new SnowCoveredForest())));
        harness.setHand(player1, new ArrayList<>());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Ohran Viper destroys an attacker it damages while blocking")
    void blockingViperDestroysDamagedAttacker() {
        Permanent attacker = addCreatureReady(player1, new BorealCentaur());
        attacker.setAttacking(true);
        addCreatureReady(player2, new OhranViper());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player1, "Boreal Centaur");
        harness.assertOnBattlefield(player2, "Ohran Viper");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ohran Viper does not destroy creatures damaged by another creature")
    void anotherCreaturesDamageDoesNotTriggerDestruction() {
        addCreatureReady(player1, new OhranViper());
        Permanent attacker = addCreatureReady(player1, new BorealCentaur());
        attacker.setAttacking(true);
        addCreatureReady(player2, new SimianBrawler());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player1, "Boreal Centaur");
        harness.assertOnBattlefield(player1, "Ohran Viper");
        harness.assertOnBattlefield(player2, "Simian Brawler");
    }

    @Test
    @DisplayName("The delayed destruction ability retains Ohran Viper as its source and its controller")
    void delayedDestructionRetainsSourceAndController() {
        Permanent viper = addCreatureReady(player1, new OhranViper());
        viper.setAttacking(true);
        addCreatureReady(player2, new BorealCentaur());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(viper.getId());
        harness.assertOnBattlefield(player2, "Boreal Centaur");

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.assertInGraveyard(player2, "Boreal Centaur");
    }
}
