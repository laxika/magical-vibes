package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeeperOfFables.class, GrizzlyBears.class, YouthfulKnight.class, Ornithopter.class})
class KeeperOfFablesTest extends BaseCardTest {

    private void addAttacker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
    }

    @Test
    @DisplayName("A non-Human creature dealing combat damage draws a card")
    void nonHumanCreatureDealsDamage() {
        addCreatureReady(player1, new KeeperOfFables());
        addAttacker(new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Multiple non-Human creatures dealing combat damage draw only one card")
    void multipleNonHumanCreaturesDrawOnce() {
        addCreatureReady(player1, new KeeperOfFables());
        addAttacker(new GrizzlyBears());
        addAttacker(new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("A Human creature dealing combat damage does not draw a card")
    void humanCreatureDoesNotTrigger() {
        addCreatureReady(player1, new KeeperOfFables());
        addAttacker(new YouthfulKnight());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("A non-Human creature dealing no combat damage does not draw a card")
    void noCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new KeeperOfFables());
        addAttacker(new Ornithopter());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
