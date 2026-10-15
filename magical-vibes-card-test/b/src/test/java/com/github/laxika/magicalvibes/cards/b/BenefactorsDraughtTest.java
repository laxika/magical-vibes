package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenefactorsDraught.class, GrizzlyBears.class})
class BenefactorsDraughtTest extends BaseCardTest {

    @Test
    void untapsAllCreaturesAndDrawsACard() {
        Permanent ownCreature = addReady(player1);
        Permanent opponentCreature = addReady(player2);
        ownCreature.tap();
        opponentCreature.tap();

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castBenefactorsDraught();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsWhenAnOpponentCreatureBlocks() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        castBenefactorsDraught();

        Permanent attacker = addReady(player1);
        attacker.setAttacking(true);
        Permanent blocker = addReady(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotDrawWhenYourCreatureBlocks() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castBenefactorsDraught();

        Permanent attacker = addReady(player2);
        attacker.setAttacking(true);
        Permanent blocker = addReady(player1);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker))));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void castBenefactorsDraught() {
        harness.setHand(player1, List.of(new BenefactorsDraught()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
