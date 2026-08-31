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

    private void castRaider() {
        harness.setHand(player1, List.of(new FireNationRaider()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
