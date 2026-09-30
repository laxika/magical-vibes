package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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
        harness.addToBattlefield(player1, new BlightwingWhelp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent whelp = findPermanent(player1, "Blightwing Whelp");
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(whelp.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Combat damage seeks a card matching the defending player's poison counters")
    void combatDamageSeeksMatchingManaValue() {
        GrayOgre matchingCard = new GrayOgre();
        GrizzlyBears nonmatchingCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonmatchingCard, matchingCard));
        gd.playerPoisonCounters.put(player2.getId(), 2);

        Permanent whelp = addCreatureReady(player1, new BlightwingWhelp());
        whelp.setAttacking(true);

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
        gd.playerPoisonCounters.put(player2.getId(), 2);

        Permanent whelp = addCreatureReady(player1, new BlightwingWhelp());
        whelp.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }
}
