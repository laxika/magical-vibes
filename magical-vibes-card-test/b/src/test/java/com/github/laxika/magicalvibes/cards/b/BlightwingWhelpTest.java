package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlightwingWhelp.class, GrayOgre.class, GrizzlyBears.class})
class BlightwingWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("The black mana ability grants haste until end of turn")
    void blackManaAbilityGrantsHaste() {
        Permanent whelp = harness.addToBattlefieldAndReturn(player1, new BlightwingWhelp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(whelp.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste expires at cleanup")
    void hasteExpiresAtCleanup() {
        Permanent whelp = harness.addToBattlefieldAndReturn(player1, new BlightwingWhelp());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, whelp, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, whelp, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Toxic gives poison during combat damage before the seek trigger resolves")
    void toxicAppliesBeforeTriggerResolution() {
        BlightwingWhelp libraryCard = new BlightwingWhelp();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());
        gd.playerPoisonCounters.put(player2.getId(), 2);
        addCreatureReady(player1, new BlightwingWhelp());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Seek uses the damaged player's poison count at resolution")
    void seekUsesPoisonCountAtResolution() {
        BlightwingWhelp matchingCard = new BlightwingWhelp();
        harness.setLibrary(player1, List.of(matchingCard));
        harness.setHand(player1, List.of());
        addCreatureReady(player1, new BlightwingWhelp());

        declareAttackers(List.of(0));
        resolveCombat();
        gd.playerPoisonCounters.put(player2.getId(), 3);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat damage seeks a card matching the defending player's poison counters")
    void combatDamageSeeksMatchingManaValue() {
        GrayOgre matchingCard = new GrayOgre();
        GrizzlyBears nonmatchingCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonmatchingCard, matchingCard));
        harness.setHand(player1, List.of());
        gd.playerPoisonCounters.put(player2.getId(), 2);

        Permanent whelp = addCreatureReady(player1, new BlightwingWhelp());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(whelp)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).contains(matchingCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatchingCard);
    }

    @Test
    @DisplayName("Combat damage does not seek when no card has the required mana value")
    void combatDamageSkipsWithoutMatchingCard() {
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());
        gd.playerPoisonCounters.put(player2.getId(), 2);

        Permanent whelp = addCreatureReady(player1, new BlightwingWhelp());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(whelp)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }
}
