package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MutinyTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's creature deals its power to another creature that player controls")
    void opponentCreatureDealsPowerDamageToAnotherCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mutiny()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID sourceId = harness.getPermanentId(player2, "Hill Giant");
        UUID victimId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(sourceId, victimId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The second target must be controlled by the first target's controller")
    void rejectsSecondTargetControlledByAnotherPlayer() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mutiny()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID sourceId = harness.getPermanentId(player2, "Hill Giant");
        UUID victimId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(sourceId, victimId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
