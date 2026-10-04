package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PumpkinBombardment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExtractPower.class, Forest.class, GrizzlyBears.class, Island.class, PumpkinBombardment.class})
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

    @Test
    void emptyLibrariesDoNotPreventResolution() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        castExtractPower();

        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Extract Power");
    }

    @Test
    void creatureCannotBeCastOutsideMainPhase() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(creature));
        castExtractPower();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }

    @Test
    void ownerCannotUseCastersPermission() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(creature));
        castExtractPower();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player2, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }

    @Test
    void playingOneExiledLandDoesNotAllowASecondLandPlay() {
        Card firstLand = new Island();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand));
        harness.setLibrary(player2, List.of(secondLand));
        castExtractPower();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, firstLand.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, secondLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.findExiledCard(secondLand.getId())).isNotNull();
    }

    @Test
    void freePlayPermissionSurvivesUntilALaterTurn() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(creature, new Forest(), new Forest(), new Forest()));
        castExtractPower();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(creature.getId())).isNull();
    }

    @Test
    void canPayAdditionalManaCostOfAnExiledSpell() {
        Card spell = new PumpkinBombardment();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(spell));
        harness.addToBattlefield(player2, new GrizzlyBears());
        castExtractPower();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromExile(player1, spell.getId(), harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Pumpkin Bombardment");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void castExtractPower() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ExtractPower(), "{5}{U}");
        harness.passBothPriorities();
    }
}
