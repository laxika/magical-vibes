package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NornsFetchling.class, Plains.class})
class NornsFetchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Without corrupted, entering conjures Plains")
    void withoutCorruptedConjuresPlains() {
        castFetchling();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Plains");
    }

    @Test
    @DisplayName("With corrupted, declining the seek conjures Plains")
    void decliningSeekConjuresPlains() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        castFetchling();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Plains");
    }

    @Test
    @DisplayName("With corrupted, accepting the seek puts a nonland card into hand")
    void acceptingSeekPutsNonlandIntoHand() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        Card creature = new NornsFetchling();
        harness.setLibrary(player1, List.of(creature));

        castFetchling();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Norn's Fetchling");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Two opposing poison counters do not enable seeking")
    void twoPoisonCountersConjurePlains() {
        gd.playerPoisonCounters.put(player2.getId(), 2);
        castFetchling();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Plains");
    }

    @Test
    @DisplayName("The controller's poison counters do not enable seeking")
    void controllersPoisonDoesNotEnableSeek() {
        gd.playerPoisonCounters.put(player1.getId(), 3);
        castFetchling();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Plains");
    }

    @Test
    @DisplayName("Seeking takes only a nonland and preserves the remaining library order")
    void seekingLeavesLandsInOrder() {
        gd.playerPoisonCounters.put(player2.getId(), 4);
        Card firstLand = new Plains();
        Card creature = new NornsFetchling();
        Card secondLand = new Plains();
        harness.setLibrary(player1, List.of(firstLand, creature, secondLand));

        castFetchling();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstLand, secondLand);
    }

    @Test
    @DisplayName("Choosing seek with only lands does not conjure Plains as a fallback")
    void seekWithNoNonlandsDoesNothing() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        Card land = new Plains();
        harness.setLibrary(player1, List.of(land));

        castFetchling();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Choosing seek with an empty library does not conjure Plains")
    void seekWithEmptyLibraryDoesNothing() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setLibrary(player1, List.of());

        castFetchling();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Corrupted becoming true before the trigger resolves enables seeking")
    void corruptedIsCheckedAtResolution() {
        harness.castFromHand(player1, new NornsFetchling(), "{1}{W}");
        harness.passBothPriorities();
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Plains");
    }

    @Test
    @DisplayName("Losing corrupted before the trigger resolves still conjures Plains")
    void losingCorruptedBeforeResolutionConjuresPlains() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.castFromHand(player1, new NornsFetchling(), "{1}{W}");
        harness.passBothPriorities();
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Plains");
    }

    @Test
    @DisplayName("Unblocked combat damage gives one poison counter in addition to life loss")
    void combatDamageAppliesToxic() {
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player2.getId(), 0);
        addCreatureReady(player1, new NornsFetchling()).setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
    }

    private void castFetchling() {
        harness.castFromHand(player1, new NornsFetchling(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
