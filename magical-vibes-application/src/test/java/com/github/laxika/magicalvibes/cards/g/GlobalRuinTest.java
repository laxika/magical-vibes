package com.github.laxika.magicalvibes.cards.g;

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

class GlobalRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Each player keeps one land of each basic land type and sacrifices the rest")
    void eachPlayerKeepsOneLandOfEachBasicLandType() {
        Permanent p1KeptForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent p1SacrificedForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent p1Island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent p1NonBasicLand = harness.addToBattlefieldAndReturn(player1, new IsolatedChapel());

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
}
