package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MistCloakedHerald;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeafloorOracle.class, MistCloakedHerald.class, RaptorCompanion.class, Forest.class})
class SeafloorOracleTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when another Merfolk you control deals combat damage to a player")
    void drawsWhenAnotherMerfolkDealsCombatDamage() {
        addCreatureReady(player1, new SeafloorOracle());
        addAttacker(new MistCloakedHerald());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws a card when Seafloor Oracle deals combat damage to a player")
    void drawsWhenOracleDealsCombatDamage() {
        addAttacker(new SeafloorOracle());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when a non-Merfolk creature deals combat damage")
    void doesNotDrawForNonMerfolk() {
        addCreatureReady(player1, new SeafloorOracle());
        addAttacker(new RaptorCompanion());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each Merfolk that deals combat damage triggers a separate draw")
    void drawsForEachMerfolkDealer() {
        addAttacker(new SeafloorOracle());
        addAttacker(new MistCloakedHerald());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each Oracle triggers independently for the same Merfolk")
    void multipleOraclesEachDraw() {
        addCreatureReady(player1, new SeafloorOracle());
        addCreatureReady(player1, new SeafloorOracle());
        addAttacker(new MistCloakedHerald());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opposing Merfolk does not trigger the Oracle")
    void doesNotDrawForOpponentsMerfolk() {
        addCreatureReady(player1, new SeafloorOracle());
        Permanent attacker = addCreatureReady(player2, new MistCloakedHerald());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not trigger a draw")
    void doesNotDrawForDamageToBlocker() {
        Permanent oracle = addAttacker(new SeafloorOracle());
        Permanent blocker = addCreatureReady(player2, new RaptorCompanion());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(oracle.getId());
        oracle.setBlockedThisCombat(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oracle.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Oracle still triggers when it dies in the same combat damage step")
    void triggersWhenOracleDiesSimultaneously() {
        Permanent oracle = addAttacker(new SeafloorOracle());
        addAttacker(new MistCloakedHerald());
        Permanent blocker = addCreatureReady(player2, new RaptorCompanion());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(oracle.getId());
        oracle.setBlockedThisCombat(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(oracle);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oracle.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent addAttacker(Card card) {
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setAttacking(true);
        return permanent;
    }
}
