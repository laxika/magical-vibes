package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExtractPower.class, Forest.class, GrizzlyBears.class, Island.class})
class ExtractPowerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card of each library face down with persistent free-play permission")
    void exilesTopCardOfEachLibraryFaceDownWithFreePlayPermission() {
        Card ownTop = new GrizzlyBears();
        Card opponentTop = new Island();
        Forest ownRemainder = new Forest();
        Forest opponentRemainder = new Forest();
        harness.setLibrary(player1, List.of(ownTop, ownRemainder));
        harness.setLibrary(player2, List.of(opponentTop, opponentRemainder));

        castExtractPower();

        ExiledCardEntry ownEntry = gd.findExiledCard(ownTop.getId());
        ExiledCardEntry opponentEntry = gd.findExiledCard(opponentTop.getId());
        assertThat(ownEntry).isNotNull();
        assertThat(opponentEntry).isNotNull();
        assertThat(ownEntry.faceDown()).isTrue();
        assertThat(opponentEntry.faceDown()).isTrue();
        assertThat(ownEntry.exilerId()).isEqualTo(player1.getId());
        assertThat(opponentEntry.exilerId()).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(ownTop.getId(), player1.getId())
                .containsEntry(opponentTop.getId(), player1.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost)
                .contains(ownTop.getId(), opponentTop.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .doesNotContain(ownTop.getId(), opponentTop.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownRemainder);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentRemainder);
    }

    @Test
    @DisplayName("Casts an opponent-owned exiled creature without mana")
    void castsOpponentOwnedExiledCreatureWithoutMana() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(creature));
        harness.setLibrary(player1, List.of());

        castExtractPower();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(creature.getId())).isNull();
    }

    @Test
    @DisplayName("Plays an exiled land without paying a mana cost")
    void playsExiledLandWithoutPayingMana() {
        Card land = new Island();
        harness.setLibrary(player2, List.of(land));
        harness.setLibrary(player1, List.of());

        castExtractPower();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.findExiledCard(land.getId())).isNull();
    }

    private void castExtractPower() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ExtractPower(), "{5}{U}");
        harness.passBothPriorities();
    }
}
