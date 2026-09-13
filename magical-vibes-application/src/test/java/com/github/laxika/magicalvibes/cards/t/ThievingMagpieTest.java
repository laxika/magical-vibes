package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThievingMagpie.class, TormentedAngel.class})
class ThievingMagpieTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it deals combat damage to an opponent")
    void drawsOnCombatDamageToOpponent() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ThievingMagpie()));
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ThievingMagpie());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Thieving Magpie");
    }

    @Test
    @DisplayName("Does not draw when blocked and no combat damage reaches the opponent")
    void doesNotDrawWhenBlocked() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ThievingMagpie()));
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new ThievingMagpie());
        Permanent blocker = addCreatureReady(player2, new TormentedAngel());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
