package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FatedClash.class, GrizzlyBears.class})
class FatedClashTest extends BaseCardTest {

    @Test
    @DisplayName("Protects the two targets before destroying all other creatures")
    void protectsTargetsBeforeBoardWipe() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new FatedClash()));
        addFullMana();
        harness.castSorcery(player1, 0, List.of(ownTarget.getId(), opponentTarget.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Allows the alternate flash cast only while a creature attacks and a creature blocks")
    void conditionalFlashCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FatedClash()));
        addFullMana();

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(ownTarget.getId(), opponentTarget.getId())))
                .isInstanceOf(IllegalStateException.class);

        opponentTarget.setAttacking(true);
        opponentTarget.setAttackTarget(player1.getId());
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(opponentTarget.getId());

        harness.getGameService().playCardWithAlternateCost(
                gd, player1, 0, 0, null, null, List.of(ownTarget.getId(), opponentTarget.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void addFullMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
