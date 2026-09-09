package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StealTheShowTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards any number, then draws that many")
    void targetPlayerDiscardsAnyNumberThenDrawsThatMany() {
        harness.setHand(player1, List.of(new StealTheShow()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.setLibrary(player2, List.of(new Shock(), new Divination()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0},
                List.of(player2.getId()), null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Shock", "Divination");
    }

    @Test
    @DisplayName("Damage mode counts instant and sorcery cards in your graveyard")
    void damageModeCountsInstantAndSorceryCardsInYourGraveyard() {
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(
                new Shock(), new Divination(), new GrizzlyBears()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        java.util.UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new StealTheShow()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(targetId), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Both modes resolve with their separate targets")
    void bothModesResolveWithSeparateTargets() {
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(new Shock(), new Divination()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        java.util.UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new StealTheShow()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), creatureId), null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Shock");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
