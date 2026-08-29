package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

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

    private Permanent addReadyServant(Player player) {
        Permanent servant = new Permanent(new ServantOfTheStinger());
        servant.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(servant);
        return servant;
    }

    private void commitCrime() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
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
