package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IsolatedChapel;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Planar Overlay")
class PlanarOverlayTest extends BaseCardTest {

    @Test
    @DisplayName("Each player returns one land of each basic land type they control")
    void eachPlayerReturnsOneLandOfEachBasicLandType() {
        Permanent player1ReturnedForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player1RemainingForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player1Island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent player1NonbasicLand = harness.addToBattlefieldAndReturn(player1, new IsolatedChapel());

        Permanent player2ReturnedMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent player2RemainingMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent player2Forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent player2NonbasicLand = harness.addToBattlefieldAndReturn(player2, new IsolatedChapel());

        harness.setHand(player1, List.of(new PlanarOverlay()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactly(
                player1ReturnedForest.getId(), player1RemainingForest.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(player1ReturnedForest.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(player1ReturnedForest, player1RemainingForest, player1Island, player1NonbasicLand);

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactly(
                player2ReturnedMountain.getId(), player2RemainingMountain.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(player2ReturnedMountain.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(player1RemainingForest.getId(), player1NonbasicLand.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(player2RemainingMountain.getId(), player2NonbasicLand.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(player1ReturnedForest.getCard(), player1Island.getCard());
        assertThat(gd.playerHands.get(player2.getId()))
                .contains(player2ReturnedMountain.getCard(), player2Forest.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(player1ReturnedForest, player1Island);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(player2ReturnedMountain, player2Forest);
    }
}
