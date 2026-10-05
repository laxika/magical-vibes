package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;




@CardUsed({OhranFrostfang.class, GrizzlyBears.class, Forest.class})
class OhranFrostfangTest extends BaseCardTest {

    @Test
    void attackingCreaturesYouControlHaveDeathtouch() {
        Permanent frostfang = addCreatureReady(player1, new OhranFrostfang());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.DEATHTOUCH)).isFalse();

        attacker.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.DEATHTOUCH)).isFalse();

        frostfang.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, frostfang, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void drawsWhenAControlledCreatureDealsCombatDamageToAPlayer() {
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        addCreatureReady(player1, new OhranFrostfang());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }
    @Test
    @DisplayName("Draws a card when a creature you control deals combat damage to a player")
    void drawsForAllyCombatDamage() {
        harness.addToBattlefield(player1, new OhranFrostfang());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void onlyControlledAttackersGainDeathtouchAndOnlyWhileAttacking() {
        Permanent frostfang = addCreatureReady(player1, new OhranFrostfang());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        opponent.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.DEATHTOUCH)).isFalse();

        attacker.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isFalse();

        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).remove(frostfang);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void drawsOnceForEachCreatureIncludingFrostfang() {
        Permanent frostfang = addCreatureReady(player1, new OhranFrostfang());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        frostfang.setAttacking(true);
        first.setAttacking(true);
        second.setAttacking(true);
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        Card thirdDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.setHand(player1, List.of());
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    void opponentsCombatDamageDoesNotTriggerDraw() {
        addCreatureReady(player1, new OhranFrostfang());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        Card undrawn = new Forest();
        harness.setLibrary(player1, List.of(undrawn));
        harness.setHand(player1, List.of());

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    void combatDamageToABlockerKillsWithDeathtouchButDoesNotDraw() {
        addCreatureReady(player1, new OhranFrostfang());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new OhranFrostfang());
        Card undrawn = new Forest();
        harness.setLibrary(player1, List.of(undrawn));
        harness.setHand(player1, List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player2, "Ohran Frostfang");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    void queuedDrawResolvesAfterFrostfangLeavesTheBattlefield() {
        Permanent frostfang = addCreatureReady(player1, new OhranFrostfang());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(frostfang);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

}
