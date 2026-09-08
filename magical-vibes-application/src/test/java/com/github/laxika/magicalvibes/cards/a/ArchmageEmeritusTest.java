package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArchmageEmeritusTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant with Archmage Emeritus draws a card")
    void castingInstantDrawsCard() {
        addCreatureReady(player1, new ArchmageEmeritus());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        GiantGrowth drawnCard = new GiantGrowth();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Copying an instant with Archmage Emeritus draws a card")
    void copyingInstantDrawsCard() {
        addCreatureReady(player1, new ArchmageEmeritus());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        GiantGrowth firstDrawnCard = new GiantGrowth();
        GrizzlyBears secondDrawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.setLibrary(player1, List.of(firstDrawnCard, secondDrawnCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(),
                List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        for (int i = 0; i < 6 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDrawnCard, secondDrawnCard);
    }
}
