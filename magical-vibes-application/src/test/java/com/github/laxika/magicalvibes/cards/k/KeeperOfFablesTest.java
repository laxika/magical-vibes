package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RagingRedcap;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeeperOfFables.class, GrizzlyBears.class, YouthfulKnight.class, Ornithopter.class,
        RagingRedcap.class})
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

    @Test
    @DisplayName("Keeper of Fables triggers from its own combat damage")
    void ownCombatDamageDrawsCard() {
        addAttacker(new KeeperOfFables());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Each Keeper draws once when multiple non-Human creatures deal damage")
    void multipleKeepersEachTriggerOnce() {
        addAttacker(new KeeperOfFables());
        addAttacker(new KeeperOfFables());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("An opponent's non-Human combat damage does not trigger Keeper")
    void opponentsCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new KeeperOfFables());
        Permanent attacker = addCreatureReady(player2, new RagingRedcap());
        attacker.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        harness.passUntil(player2, TurnStep.END_OF_COMBAT);

        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Double strike triggers Keeper once in each combat damage step")
    void doubleStrikeDrawsTwoCards() {
        addCreatureReady(player1, new KeeperOfFables());
        addAttacker(new RagingRedcap());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }
}
