package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CoastalTower;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TropicalIsland;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlobalRuin.class, Forest.class, Island.class, Mountain.class, CoastalTower.class,
        TropicalIsland.class})
class GlobalRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Each player keeps one land of each basic land type and sacrifices the rest")
    void eachPlayerKeepsOneLandOfEachBasicLandType() {
        Permanent p1KeptForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent p1SacrificedForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent p1Island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent p1NonBasicLand = harness.addToBattlefieldAndReturn(player1, new CoastalTower());

        Permanent p2KeptMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent p2SacrificedMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent p2Forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        GlobalRuin globalRuin = new GlobalRuin();
        harness.setHand(player1, List.of(globalRuin));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactly(p1KeptForest.getId(), p1SacrificedForest.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(p1KeptForest.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(
                        p1KeptForest.getId(), p1SacrificedForest.getId(), p1Island.getId(), p1NonBasicLand.getId());

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactly(p2KeptMountain.getId(), p2SacrificedMountain.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(p2KeptMountain.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(p1KeptForest.getId(), p1Island.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(p2KeptMountain.getId(), p2Forest.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(globalRuin, p1SacrificedForest.getCard(), p1NonBasicLand.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(p2SacrificedMountain.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(p1SacrificedForest.getId(), p1NonBasicLand.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(p2SacrificedMountain.getId());
    }

    @Test
    @DisplayName("A land with multiple basic land types can be kept for both types")
    void keepsMultiTypeLandForEachBasicLandType() {
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent tropicalIsland = harness.addToBattlefieldAndReturn(player1, new TropicalIsland());

        GlobalRuin globalRuin = new GlobalRuin();
        harness.setHand(player1, List.of(globalRuin));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice forestChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(forestChoice).isNotNull();
        assertThat(forestChoice.validIds())
                .containsExactly(firstForest.getId(), secondForest.getId(), tropicalIsland.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(tropicalIsland.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(tropicalIsland.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(globalRuin, firstForest.getCard(), secondForest.getCard());
    }

    @Test
    @DisplayName("Sacrifices every land when no player controls a land with a basic land type")
    void sacrificesAllLandsWithoutBasicLandTypes() {
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new CoastalTower());
        Permanent player2Land = harness.addToBattlefieldAndReturn(player2, new CoastalTower());

        GlobalRuin globalRuin = new GlobalRuin();
        harness.setHand(player1, List.of(globalRuin));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(globalRuin, player1Land.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(player2Land.getCard());
    }

    @Test
    @DisplayName("Players choose in active-player order before any land is sacrificed")
    void choosesInActivePlayerOrder() {
        Permanent player1Mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent player1OtherMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent player2Forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent player2OtherForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        GlobalRuin globalRuin = new GlobalRuin();
        harness.setHand(player2, List.of(globalRuin));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player2.getId());
        assertThat(firstChoice.validIds()).containsExactly(player2Forest.getId(), player2OtherForest.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(player2Forest.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player1.getId());
        assertThat(secondChoice.validIds()).containsExactly(player1Mountain.getId(), player1OtherMountain.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(player1Mountain.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(player1Mountain.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(player2Forest.getId());
    }
}
