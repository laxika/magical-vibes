package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulsFireTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen creature deals damage equal to its power to a target creature, killing it")
    void creatureKillsTargetCreature() {
        // Hill Giant (3/3) deals 3 damage to Grizzly Bears (2/2)
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(giantId, bearsId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Chosen creature deals damage equal to its power to a target player")
    void creatureDamagesTargetPlayer() {
        // Hill Giant (3/3) deals 3 damage to the opponent
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, List.of(giantId, player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot choose an opponent's creature as the damage source")
    void cannotChooseOpponentCreatureAsSource() {
        harness.addToBattlefield(player1, new HillGiant()); // gives a legal source so the spell is castable
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID opponentGiantId = harness.getPermanentId(player2, "Hill Giant");
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opponentGiantId, targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }
}
