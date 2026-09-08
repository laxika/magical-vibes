package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BigfinBouncerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns target creature an opponent controls to its owner's hand")
    void etbBouncesOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castBigfinBouncer(harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Bigfin Bouncer");
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new BigfinBouncer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownCreatureId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castBigfinBouncer(UUID targetId) {
        harness.setHand(player1, List.of(new BigfinBouncer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
