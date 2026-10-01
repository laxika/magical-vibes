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
}
