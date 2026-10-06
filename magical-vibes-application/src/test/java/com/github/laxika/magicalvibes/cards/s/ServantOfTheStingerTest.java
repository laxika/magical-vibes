package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ServantOfTheStinger.class, Shock.class, Forest.class})
class ServantOfTheStingerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage only offers the search after committing a crime")
    void searchesAfterCrimeAndCombatDamage() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent servant = addReadyServant(player1);
        servant.setAttacking(true);

        commitCrime();
        resolveUnblockedCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Servant of the Stinger");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("The ability does not trigger when no crime was committed")
    void doesNotTriggerWithoutCrime() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent servant = addReadyServant(player1);
        servant.setAttacking(true);

        resolveUnblockedCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Servant of the Stinger");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the sacrifice keeps the creature and skips the search")
    void declineSacrifice() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent servant = addReadyServant(player1);
        servant.setAttacking(true);

        commitCrime();
        resolveUnblockedCombat();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Servant of the Stinger");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("An empty library does not prevent sacrificing the creature")
    void sacrificesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent servant = addReadyServant(player1);
        servant.setAttacking(true);

        commitCrime();
        resolveUnblockedCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Servant of the Stinger");
        harness.assertNotOnBattlefield(player1, "Servant of the Stinger");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Targeting yourself does not enable the combat damage ability")
    void targetingYourselfIsNotACrime() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent servant = addReadyServant(player1);
        servant.setAttacking(true);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        resolveUnblockedCombat();

        harness.assertOnBattlefield(player1, "Servant of the Stinger");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's crime does not enable your combat damage ability")
    void opponentsCrimeDoesNotEnableSearch() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent servant = addReadyServant(player1);
        servant.setAttacking(true);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        resolveUnblockedCombat();

        harness.assertOnBattlefield(player1, "Servant of the Stinger");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The unrestricted search can find a nonland card")
    void searchesForNonlandCard() {
        harness.setLibrary(player1, List.of(new ServantOfTheStinger(), new Forest()));
        Permanent servant = addReadyServant(player1);
        servant.setAttacking(true);

        commitCrime();
        resolveUnblockedCombat();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Servant of the Stinger");
        harness.assertInHand(player1, "Servant of the Stinger");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Removing the source before resolution prevents the search")
    void cannotSearchWhenSourceHasLeftBattlefield() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent servant = addReadyServant(player1);
        commitCrime();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        servant.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player2, 0, servant.getId());
        harness.castAndResolveInstant(player2, 0, servant.getId());
        harness.assertInGraveyard(player1, "Servant of the Stinger");
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyServant(Player player) {
        Permanent servant = harness.addToBattlefieldAndReturn(player, new ServantOfTheStinger());
        servant.setSummoningSick(false);
        return servant;
    }

    private void commitCrime() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }

    private void resolveUnblockedCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
