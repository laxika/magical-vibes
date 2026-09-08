package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GnarlbackRhinoTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when you cast a spell targeting Gnarlback Rhino")
    void drawsWhenTargetedByOwnSpell() {
        harness.addToBattlefield(player1, new GnarlbackRhino());
        UUID rhinoId = harness.getPermanentId(player1, "Gnarlback Rhino");

        harness.setHand(player1, List.of(new Shock()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, rhinoId);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Does not draw a card when you cast a spell targeting another creature")
    void doesNotDrawWhenAnotherCreatureIsTargeted() {
        harness.addToBattlefield(player1, new GnarlbackRhino());
        UUID otherCreatureId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new Shock()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, otherCreatureId);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);
    }
}
