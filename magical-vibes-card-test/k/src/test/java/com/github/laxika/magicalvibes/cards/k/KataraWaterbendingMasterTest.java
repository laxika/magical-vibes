package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KataraWaterbendingMaster.class, Forest.class, Shock.class})
class KataraWaterbendingMasterTest extends BaseCardTest {

    @Test
    void getsExperienceWhenControllerCastsDuringOpponentTurn() {
        harness.addToBattlefield(player1, new KataraWaterbendingMaster());
        enterOpponentTurn();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void doesNotGetExperienceWhenControllerCastsDuringOwnTurn() {
        harness.addToBattlefield(player1, new KataraWaterbendingMaster());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
    }

    @Test
    void attackingMayDrawsForExperienceThenDiscards() {
        addReadyKatara();
        gd.playerExperienceCounters.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void opponentCastingDuringTheirTurnDoesNotGiveExperience() {
        harness.addToBattlefield(player1, new KataraWaterbendingMaster());
        enterOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).isEmpty();
    }

    @Test
    void eachSpellDuringOpponentTurnAddsAnotherExperienceCounter() {
        harness.addToBattlefield(player1, new KataraWaterbendingMaster());
        enterOpponentTurn();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 2);
        assertThat(gd.playerExperienceCounters).doesNotContainKey(player2.getId());
    }

    @Test
    void decliningAttackAbilityNeitherDrawsNorDiscards() {
        addReadyKatara();
        gd.playerExperienceCounters.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningWithNoExperienceKeepsHandAndLibrary() {
        addReadyKatara();
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Shock");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attackAbilityUsesExperienceCountAtResolution() {
        addReadyKatara();
        gd.playerExperienceCounters.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        declareAttackers(player1, List.of(0));
        gd.playerExperienceCounters.put(player1.getId(), 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void enterOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addReadyKatara() {
        addCreatureReady(player1, new KataraWaterbendingMaster());
    }
}
