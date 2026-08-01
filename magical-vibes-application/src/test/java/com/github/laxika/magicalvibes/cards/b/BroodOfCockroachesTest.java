package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedLoseLifeAndReturnFromGraveyard;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BroodOfCockroachesTest extends BaseCardTest {

    @Test
    @DisplayName("Death registers a delayed trigger; no return or life loss yet")
    void deathRegistersDelayedTrigger() {
        Permanent brood = harness.addToBattlefieldAndReturn(player1, new BroodOfCockroaches());
        UUID broodId = brood.getCard().getId();
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, brood.getId());
        harness.passBothPriorities(); // Bolt resolves — Brood dies
        harness.passBothPriorities(); // death trigger registers delayed effect

        harness.assertInGraveyard(player1, "Brood of Cockroaches");
        assertThat(gd.getDelayedActions(DelayedLoseLifeAndReturnFromGraveyard.class)).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(broodId));
    }

    @Test
    @DisplayName("At next end step, controller loses 1 life and Brood returns to hand")
    void losesLifeAndReturnsAtNextEndStep() {
        Permanent brood = harness.addToBattlefieldAndReturn(player1, new BroodOfCockroaches());
        UUID broodId = brood.getCard().getId();
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, brood.getId());
        harness.passBothPriorities(); // Bolt resolves
        harness.passBothPriorities(); // register delayed

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities(); // resolve delayed trigger

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(broodId));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(broodId));
        assertThat(gd.getDelayedActions(DelayedLoseLifeAndReturnFromGraveyard.class)).isEmpty();
    }
}
