package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NestOfScarabsTest extends BaseCardTest {

    /** Drives the stack to completion (Nest's tokens are mandatory — no prompts). Bounded so a stuck
     *  state fails fast instead of hanging. */
    private void resolveStack() {
        for (int guard = 0; guard < 40 && !gd.stack.isEmpty(); guard++) {
            harness.passBothPriorities();
        }
    }

    private long insectTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Insect"))
                .count();
    }

    @Test
    @DisplayName("You put three -1/-1 counters on a creature — create three Insect tokens")
    void createsThatManyTokensWhenYouPlaceCounters() {
        harness.addToBattlefield(player1, new NestOfScarabs());
        // 4/4 survives three -1/-1 counters (becomes 1/1), so no death interferes.
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        // player1 casts Skinrender → player1 puts three -1/-1 counters → Nest triggers.
        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
        resolveStack();

        assertThat(insectTokenCount(player1)).isEqualTo(3);
        assertThat(insectTokenCount(player2)).isZero();
    }

    @Test
    @DisplayName("An opponent putting the -1/-1 counters does not trigger your Nest of Scarabs")
    void doesNotTriggerWhenOpponentPlacesCounters() {
        harness.addToBattlefield(player1, new NestOfScarabs());
        // The creature receiving the counters belongs to player1 so player2's Skinrender has a target.
        harness.addToBattlefield(player1, new AirElemental());
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        harness.setHand(player2, List.of(new Skinrender()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        // player2 casts Skinrender → player2 (not player1) puts the counters → Nest must not trigger.
        harness.forceActivePlayer(player2);
        harness.getGameService().playCard(gd, player2, 0, 0, targetId, null);
        resolveStack();

        assertThat(insectTokenCount(player1)).isZero();
    }
}
