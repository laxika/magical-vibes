package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SongOfBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Mills four cards from library")
    void millsFourCards() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new GrizzlyBears(), new Mountain(), new GrizzlyBears(), new Mountain(),
                new Mountain()));

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5); // 4 milled + Song of Blood
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c instanceof GrizzlyBears || c instanceof Mountain)
                .count()).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking creature gets +1/+0 per creature card milled")
    void attackerBoostedPerMilledCreature() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new GrizzlyBears(), new Mountain(), new GrizzlyBears(), new Mountain()));

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        castAndResolve();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        declareAttackers(List.of(0));
        resolveStack();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("No boost when no creature cards are milled")
    void noBoostWithoutMilledCreatures() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new Mountain(), new Mountain(), new Mountain(), new Mountain()));

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        castAndResolve();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Opponent's attacker also gets the boost")
    void boostsAnyCreatureThatAttacks() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new GrizzlyBears(), new Mountain(), new Mountain(), new Mountain()));

        Permanent oppAttacker = new Permanent(new GrizzlyBears());
        oppAttacker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppAttacker);

        castAndResolve();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        declareAttackers(player2, List.of(0));
        resolveStack();

        assertThat(oppAttacker.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacker boost wears off at end of turn")
    void boostExpiresAtEndOfTurn() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new GrizzlyBears(), new Mountain(), new Mountain(), new Mountain()));

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        castAndResolve();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        declareAttackers(List.of(0));
        resolveStack();
        assertThat(attacker.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new SongOfBlood()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }
}
