package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FireNationRaider.class)
class FireNationRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Raid creates a Clue when Fire Nation Raider enters after an attack")
    void raidCreatesClueAfterAttack() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        castRaider();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Raid does not create a Clue when its controller did not attack")
    void raidDoesNotCreateClueWithoutAttack() {
        castRaider();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's attack does not satisfy raid")
    void opponentAttackDoesNotSatisfyRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());

        castRaider();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("The raid Clue can be sacrificed for two mana to draw a card")
    void clueCanBeSacrificedToDraw() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castRaider();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new FireNationRaider()));
        int handSize = gd.playerHands.get(player1.getId()).size();
        var clue = findPermanents(player1, "Clue").getFirst();
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, clueIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        harness.assertInHand(player1, "Fire Nation Raider");
    }

    @Test
    @DisplayName("Raid still creates a Clue after the Raider leaves the battlefield")
    void raidResolvesWithoutSource() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castRaider();
        harness.passBothPriorities();
        var raider = findPermanent(player1, "Fire Nation Raider");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, raider));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fire Nation Raider");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }
    private void castRaider() {
        harness.castFromHand(player1, new FireNationRaider(), "{3}{R}");
    }
}
