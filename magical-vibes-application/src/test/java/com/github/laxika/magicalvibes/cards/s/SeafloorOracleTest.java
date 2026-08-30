package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkSovereign;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeafloorOracleTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when another Merfolk you control deals combat damage to a player")
    void drawsWhenAnotherMerfolkDealsCombatDamage() {
        addReadyCreature(player1, new SeafloorOracle());
        addAttacker(new MerfolkSovereign());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombatDamage();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws a card when Seafloor Oracle deals combat damage to a player")
    void drawsWhenOracleDealsCombatDamage() {
        addAttacker(new SeafloorOracle());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombatDamage();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when a non-Merfolk creature deals combat damage")
    void doesNotDrawForNonMerfolk() {
        addReadyCreature(player1, new SeafloorOracle());
        addAttacker(new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombatDamage();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addAttacker(Card card) {
        Permanent permanent = addReadyCreature(player1, card);
        permanent.setAttacking(true);
        return permanent;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void resolveCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
