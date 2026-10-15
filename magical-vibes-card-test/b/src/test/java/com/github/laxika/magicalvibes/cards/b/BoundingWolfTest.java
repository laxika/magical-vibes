package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.StormriderSpirit;
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

@CardUsed({BoundingWolf.class, StormriderSpirit.class})
class BoundingWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast during combat because it has flash")
    void canBeCastDuringCombat() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new BoundingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bounding Wolf");
    }

    @Test
    @DisplayName("Can block a creature with flying because it has reach")
    void canBlockFlyingCreature() {
        Permanent flyer = addCreatureReady(player1, new StormriderSpirit());
        flyer.setAttacking(true);
        Permanent wolf = addCreatureReady(player2, new BoundingWolf());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(wolf),
                gd.playerBattlefields.get(player1.getId()).indexOf(flyer))));

        assertThat(wolf.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can flash in during an opponent's combat and immediately block a flyer")
    void canFlashInAndBlockDespiteSummoningSickness() {
        Permanent flyer = addCreatureReady(player1, new StormriderSpirit());
        flyer.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BoundingWolf()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        harness.assertOnBattlefield(player2, "Bounding Wolf");
        Permanent wolf = findPermanent(player2, "Bounding Wolf");
        assertThat(wolf.isSummoningSick()).isTrue();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(wolf),
                gd.playerBattlefields.get(player1.getId()).indexOf(flyer))));

        assertThat(wolf.isBlocking()).isTrue();
    }
}
