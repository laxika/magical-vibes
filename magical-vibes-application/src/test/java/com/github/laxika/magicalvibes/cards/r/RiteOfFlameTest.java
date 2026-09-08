package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiteOfFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Adds two red mana when no Rite of Flame is in a graveyard")
    void addsTwoRedManaWithNoCopiesInGraveyards() {
        castRiteOfFlame();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds one red mana for each Rite of Flame in all graveyards")
    void countsCopiesInAllGraveyards() {
        gd.playerGraveyards.get(player1.getId()).add(new RiteOfFlame());
        gd.playerGraveyards.get(player2.getId()).add(new RiteOfFlame());

        castRiteOfFlame();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count other cards in graveyards")
    void ignoresOtherCards() {
        gd.playerGraveyards.get(player1.getId()).add(new com.github.laxika.magicalvibes.cards.k.Kindle());
        gd.playerGraveyards.get(player2.getId()).add(new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        castRiteOfFlame();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("A resolved Rite of Flame counts for the next one")
    void resolvedCopyCountsForNextCast() {
        harness.setHand(player1, List.of(new RiteOfFlame(), new RiteOfFlame()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(5);
    }

    private void castRiteOfFlame() {
        harness.setHand(player1, List.of(new RiteOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
