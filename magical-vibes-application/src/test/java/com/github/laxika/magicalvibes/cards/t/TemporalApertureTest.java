package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gamble;
import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.h.HeatRay;
import com.github.laxika.magicalvibes.cards.r.Raze;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemporalAperture.class, DarkRitual.class, Forest.class, Gamble.class, HeatRay.class, GoblinRaider.class, Raze.class})
class TemporalApertureTest extends BaseCardTest {

    private Permanent activate() {
        Permanent temporalAperture = harness.addToBattlefieldAndReturn(player1, new TemporalAperture());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        return temporalAperture;
    }

    @Test
    @DisplayName("Activation pays five generic mana and taps the artifact")
    void activationPaysManaAndTapsArtifact() {
        DarkRitual darkRitual = new DarkRitual();
        harness.setLibrary(player1, List.of(darkRitual));

        Permanent temporalAperture = activate();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(temporalAperture.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activation reveals the top card and grants delayed free play")
    void activationGrantsFreePlayWithoutImmediateChoice() {
        DarkRitual darkRitual = new DarkRitual();
        harness.setLibrary(player1, List.of(darkRitual));

        activate();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.libraryTopCardFreePlayPermissionsUntilEndOfTurn)
                .containsEntry(player1.getId(), darkRitual.getId());
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(darkRitual);
    }

    @Test
    @DisplayName("The revealed top card is visible to both players")
    void revealsTopCardToBothPlayers() {
        DarkRitual darkRitual = new DarkRitual();
        harness.setLibrary(player1, List.of(darkRitual));

        activate();
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Dark Ritual"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Dark Ritual"));
    }

    @Test
    @DisplayName("The revealed spell can be cast later without paying mana")
    void castsRevealedSpellLaterForFree() {
        DarkRitual darkRitual = new DarkRitual();
        harness.setLibrary(player1, List.of(darkRitual));
        activate();

        harness.castFromLibraryTop(player1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Dark Ritual");
        assertThat(gd.libraryTopCardFreePlayPermissionsUntilEndOfTurn)
                .doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("The permission does not follow a card that leaves the top")
    void permissionEndsWhenCardLeavesTop() {
        DarkRitual darkRitual = new DarkRitual();
        Gamble gamble = new Gamble();
        harness.setLibrary(player1, List.of(darkRitual));
        activate();

        harness.setLibrary(player1, List.of(gamble, darkRitual));
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);

        harness.setLibrary(player1, List.of(darkRitual));
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The revealed land can be played from the top")
    void playsRevealedLandFromTop() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        activate();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A free X spell uses zero for X")
    void freeXSpellUsesZeroForX() {
        HeatRay heatRay = new HeatRay();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinRaider());
        harness.setLibrary(player1, List.of(heatRay));

        activate();

        harness.castFromLibraryTop(player1, target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Goblin Raider");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A later library shuffle ends the free-play permission")
    void laterLibraryShuffleEndsFreePlayPermission() {
        DarkRitual darkRitual = new DarkRitual();
        Forest searchedForest = new Forest();
        Gamble gamble = new Gamble();
        harness.setLibrary(player1, List.of(darkRitual));

        activate();
        harness.setLibrary(player1, List.of(darkRitual, searchedForest));
        harness.castFromHand(player1, gamble, "{R}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(darkRitual);
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The free-play permission expires at cleanup")
    void permissionExpiresAtCleanup() {
        DarkRitual darkRitual = new DarkRitual();
        harness.setLibrary(player1, List.of(darkRitual));
        activate();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gd.libraryTopCardFreePlayPermissionsUntilEndOfTurn)
                .doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("An empty library grants no free-play permission")
    void emptyLibraryGrantsNoPermission() {
        harness.setLibrary(player1, List.of());

        Permanent temporalAperture = activate();

        assertThat(temporalAperture.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.libraryTopCardFreePlayPermissionsUntilEndOfTurn)
                .doesNotContainKey(player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Free play does not let a creature be cast during combat")
    void freePlayRespectsCreatureTiming() {
        harness.setLibrary(player1, List.of(new GoblinRaider()));
        activate();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Goblin Raider");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The revealed instant can be cast on the opponent's turn")
    void freeInstantCanBeCastOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new DarkRitual()));
        activate();

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertInGraveyard(player1, "Dark Ritual");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Free play does not allow an extra land play")
    void freeLandRespectsLandPlayLimit() {
        harness.setLibrary(player1, List.of(new Forest()));
        activate();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A revealed Raze can be cast by paying its additional land sacrifice")
    void freePlayAllowsMandatoryLandSacrifice() {
        harness.setLibrary(player1, List.of(new Raze()));
        activate();
        Permanent sacrificedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        gs.playCardFromLibraryTop(gd, player1, null, targetLand.getId(),
                List.of(), List.of(sacrificedLand.getId()));

        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Raze");
        harness.assertInGraveyard(player2, "Forest");
    }
}
