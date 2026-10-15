package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JeskaiShrinekeeper.class})
class JeskaiShrinekeeperTest extends BaseCardTest {

    @Test
    void combatDamageToPlayerGainsLifeAndDrawsCard() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new JeskaiShrinekeeper()));

        addCreatureReady(player1, new JeskaiShrinekeeper()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void combatDamageRewardsTheControllerWhenPlayerTwoAttacks() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new JeskaiShrinekeeper()));
        addCreatureReady(player2, new JeskaiShrinekeeper()).setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 21);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void combatDamageToACreatureDoesNotGainLifeOrDraw() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new JeskaiShrinekeeper()));
        harness.setLibrary(player2, List.of(new JeskaiShrinekeeper()));
        addCreatureReady(player1, new JeskaiShrinekeeper());
        addCreatureReady(player2, new JeskaiShrinekeeper());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Jeskai Shrinekeeper");
        harness.assertInGraveyard(player2, "Jeskai Shrinekeeper");
    }
}
