package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FlockImpostor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnsLightArcher.class, FlockImpostor.class})
class DawnsLightArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast during the opponent's turn thanks to Flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new DawnsLightArcher()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        gs.passPriority(gd, player2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dawn's Light Archer");
    }

    @Test
    @DisplayName("Reach lets Dawn's Light Archer block a creature with flying")
    void reachCanBlockFlyer() {
        Permanent flyer = addCreatureReady(player1, new FlockImpostor());
        flyer.setAttacking(true);
        Permanent archer = addCreatureReady(player2, new DawnsLightArcher());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(archer),
                gd.playerBattlefields.get(player1.getId()).indexOf(flyer))));

        assertThat(archer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can flash in after attackers are declared and immediately block a flyer")
    void canFlashInAndBlockFlyer() {
        addCreatureReady(player1, new FlockImpostor());
        harness.setHand(player2, List.of(new DawnsLightArcher()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.ensurePriority(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dawn's Light Archer");
        Permanent archer = findPermanent(player2, "Dawn's Light Archer");
        assertThat(archer.isSummoningSick()).isTrue();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(archer.isBlocking()).isTrue();
    }
}
