package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NettlingHost.class, Nettlecyst.class})
class NettlingHostTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked combat damage deals damage and gives two poison counters")
    void combatDamageGivesTwoPoisonCounters() {
        harness.setLife(player2, 20);
        Permanent host = addCreatureReady(player1, new NettlingHost());
        host.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiling is paid immediately and corrupted is not checked again on resolution")
    void exileCostIsPaidBeforeResolutionAndPoisonIsNotRechecked() {
        harness.setHand(player1, List.of());
        NettlingHost host = new NettlingHost();
        harness.setGraveyard(player1, List.of(host));
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(host.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Nettlecyst");
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getOwnerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Corrupted exiles Nettling Host and conjures Nettlecyst into hand")
    void corruptedExilesHostAndConjuresNettlecyst() {
        harness.setHand(player1, List.of());
        NettlingHost host = new NettlingHost();
        harness.setGraveyard(player1, List.of(host));
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(host);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(host.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Nettlecyst");
    }

    @Test
    @DisplayName("Corrupted cannot be activated without an opponent having three poison counters")
    void corruptedRequiresOpponentWithThreePoisonCounters() {
        harness.setHand(player1, List.of());
        NettlingHost host = new NettlingHost();
        harness.setGraveyard(player1, List.of(host));
        gd.playerPoisonCounters.put(player1.getId(), 3);
        gd.playerPoisonCounters.put(player2.getId(), 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("An opponent must have at least 3 poison counters");

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(host);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
