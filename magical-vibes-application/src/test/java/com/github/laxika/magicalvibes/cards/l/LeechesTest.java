package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LeechesTest extends BaseCardTest {

    @Test
    @DisplayName("Removes all poison counters from target player and deals that much damage")
    void removesPoisonAndDealsDamage() {
        gd.playerPoisonCounters.put(player2.getId(), 4);

        castLeechesTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals no damage when the target has no poison counters")
    void noPoisonNoDamage() {
        castLeechesTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        gd.playerPoisonCounters.put(player1.getId(), 2);

        castLeechesTargeting(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Leaves the non-targeted player's poison counters alone")
    void doesNotAffectOtherPlayer() {
        gd.playerPoisonCounters.put(player1.getId(), 3);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        castLeechesTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        castLeechesTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Leeches");
    }

    private void castLeechesTargeting(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Leeches()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castSorcery(player1, 0, targetPlayerId);
    }
}
