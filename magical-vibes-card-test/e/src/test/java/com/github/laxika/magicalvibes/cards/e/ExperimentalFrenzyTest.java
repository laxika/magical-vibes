package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Harrow;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExperimentalFrenzy.class, Forest.class, GrizzlyBears.class, Shock.class, Harrow.class})
class ExperimentalFrenzyTest extends BaseCardTest {

    @Test
    void cannotPlayLandsOrCastSpellsFromHand() {
        harness.addToBattlefield(player1, new ExperimentalFrenzy());
        harness.setHand(player1, List.of(new Forest(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPlayLandFromTopOfLibrary() {
        harness.addToBattlefield(player1, new ExperimentalFrenzy());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void canCastSpellFromTopOfLibrary() {
        harness.addToBattlefield(player1, new ExperimentalFrenzy());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void activatedAbilityDestroysExperimentalFrenzy() {
        harness.addToBattlefield(player1, new ExperimentalFrenzy());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Experimental Frenzy");
    }

    @Test
    void topCardIsVisibleOnlyToController() {
        harness.addToBattlefield(player1, new ExperimentalFrenzy());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new ExperimentalFrenzy()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Forest")
                        && message.contains("}],[]]"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void libraryPermissionDoesNotGrantExtraLandPlays() {
        harness.addToBattlefield(player1, new ExperimentalFrenzy());
        Forest secondLand = new Forest();
        harness.setLibrary(player1, List.of(new Forest(), secondLand));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondLand);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotCastCreatureFromLibraryOutsideMainPhase() {
        harness.addToBattlefield(player1, new ExperimentalFrenzy());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void librarySpellsStillRequireManaPayment() {
        harness.addToBattlefield(player1, new ExperimentalFrenzy());
        harness.setLibrary(player1, List.of(new ExperimentalFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void canCastInstantFromLibraryOnOpponentsTurn() {
        harness.addToBattlefield(player1, new ExperimentalFrenzy());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castAndResolveFromLibraryTop(player1, player2.getId());

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void destroyingFrenzyRestoresPlayingFromHandAndRemovesLibraryPermission() {
        harness.addToBattlefield(player1, new ExperimentalFrenzy());
        harness.setHand(player1, List.of(new Forest(), new Shock()));
        Forest libraryLand = new Forest();
        harness.setLibrary(player1, List.of(libraryLand));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        harness.playLand(player1, 0);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Experimental Frenzy");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryLand);
    }

    @Test
    void canPayLandSacrificeAdditionalCostForSpellFromLibrary() {
        harness.addToBattlefield(player1, new ExperimentalFrenzy());
        harness.addToBattlefield(player1, new Forest());
        Harrow harrow = new Harrow();
        harness.setLibrary(player1, List.of(harrow));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromLibraryTopWithAdditionalCost(player1,
                harness.getPermanentId(player1, "Forest"));

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }
}
