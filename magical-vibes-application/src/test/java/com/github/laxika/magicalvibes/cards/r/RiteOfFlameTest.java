package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiteOfFlame.class, SnowCoveredMountain.class, SnowCoveredIsland.class})
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
        gd.playerGraveyards.get(player1.getId()).add(new SnowCoveredMountain());
        gd.playerGraveyards.get(player2.getId()).add(new SnowCoveredIsland());

        castRiteOfFlame();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("A resolved Rite of Flame counts for the next one")
    void resolvedCopyCountsForNextCast() {
        harness.setHand(player1, List.of(new RiteOfFlame(), new RiteOfFlame()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(5);
    }

    private void castRiteOfFlame() {
        harness.castFromHand(player1, new RiteOfFlame(), "{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Counts copies that enter a graveyard after casting")
    void countsCopiesAtResolution() {
        harness.castFromHand(player1, new RiteOfFlame(), "{R}");
        harness.setGraveyard(player2, List.of(new RiteOfFlame(), new RiteOfFlame()));

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count copies removed from a graveyard before resolution")
    void ignoresCopiesRemovedBeforeResolution() {
        RiteOfFlame graveyardCopy = new RiteOfFlame();
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.castFromHand(player1, new RiteOfFlame(), "{R}");
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(graveyardCopy));

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count copies in hand or exile")
    void ignoresCopiesOutsideGraveyards() {
        harness.setHand(player2, List.of(new RiteOfFlame()));
        harness.setExile(player1, List.of(new RiteOfFlame()));
        harness.setExile(player2, List.of(new RiteOfFlame()));

        castRiteOfFlame();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }
}
