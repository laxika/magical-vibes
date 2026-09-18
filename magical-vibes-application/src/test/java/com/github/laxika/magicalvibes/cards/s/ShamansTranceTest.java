package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EmberShot;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShamansTrance.class, EmberShot.class, KrosanVerge.class})
class ShamansTranceTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a spell from an opponent's graveyard and returns it to its owner's graveyard")
    void castsSpellFromOpponentsGraveyard() {
        ShamansTrance trance = new ShamansTrance();
        EmberShot emberShot = new EmberShot();
        harness.setGraveyard(player2, List.of(emberShot));
        harness.setLibrary(player1, List.of(new KrosanVerge()));
        prepareMainPhase(player1);

        harness.castFromHand(player1, trance, "{2}{R}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 7);
        gs.playFlashbackSpell(gd, player1, emberShot.getId(), null, player2.getId(), List.of(), List.of(), null);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInHand(player1, "Krosan Verge");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(emberShot);
        harness.assertInGraveyard(player1, "Shaman's Trance");
    }

    @Test
    @DisplayName("Allows playing a land from an opponent's graveyard")
    void playsLandFromOpponentsGraveyard() {
        ShamansTrance trance = new ShamansTrance();
        KrosanVerge krosanVerge = new KrosanVerge();
        harness.setGraveyard(player2, List.of(krosanVerge));
        prepareMainPhase(player1);

        harness.castFromHand(player1, trance, "{2}{R}");
        harness.passBothPriorities();
        harness.playGraveyardLand(player1, krosanVerge.getId());

        harness.assertOnBattlefield(player1, "Krosan Verge");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Advertises an opponent's graveyard spell as playable")
    void advertisesOpponentGraveyardSpellAsPlayable() {
        ShamansTrance trance = new ShamansTrance();
        EmberShot emberShot = new EmberShot();
        harness.setGraveyard(player2, List.of(emberShot));
        prepareMainPhase(player1);

        harness.castFromHand(player1, trance, "{2}{R}");
        harness.passBothPriorities();

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableFlashbackIndices(gd, player1.getId()))
                .as("opponent graveyard spell availability")
                .isNotEmpty();
    }

    @Test
    @DisplayName("Advertises an opponent's graveyard land as playable")
    void advertisesOpponentGraveyardLandAsPlayable() {
        ShamansTrance trance = new ShamansTrance();
        KrosanVerge krosanVerge = new KrosanVerge();
        harness.setGraveyard(player2, List.of(krosanVerge));
        prepareMainPhase(player1);

        harness.castFromHand(player1, trance, "{2}{R}");
        harness.passBothPriorities();

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableGraveyardLandIndices(gd, player1.getId()))
                .as("opponent graveyard land availability")
                .isNotEmpty();
    }

    @Test
    @DisplayName("The permission to play from other graveyards expires at end of turn")
    void permissionExpiresAtEndOfTurn() {
        ShamansTrance trance = new ShamansTrance();
        KrosanVerge firstVerge = new KrosanVerge();
        KrosanVerge secondVerge = new KrosanVerge();
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of(firstVerge, secondVerge));
        prepareMainPhase(player1);

        harness.castFromHand(player1, trance, "{2}{R}");
        harness.passBothPriorities();
        harness.playGraveyardLand(player1, firstVerge.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, secondVerge.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(secondVerge);
    }

    @Test
    @DisplayName("Prevents other players from casting spells from their graveyards")
    void preventsOtherPlayersFromCastingSpellsFromTheirGraveyards() {
        ShamansTrance trance = new ShamansTrance();
        EmberShot emberShot = new EmberShot();
        harness.setGraveyard(player2, List.of(emberShot));
        prepareMainPhase(player1);

        harness.castFromHand(player1, trance, "{2}{R}");
        harness.passBothPriorities();
        prepareMainPhase(player2);
        harness.addMana(player2, ManaColor.RED, 7);

        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player2, emberShot.getId(), null, player1.getId(), List.of(), List.of(), null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(emberShot);
    }

    @Test
    @DisplayName("Prevents other players from playing lands from their graveyards")
    void preventsOtherPlayersFromPlayingLandsFromTheirGraveyards() {
        ShamansTrance trance = new ShamansTrance();
        KrosanVerge krosanVerge = new KrosanVerge();
        harness.setGraveyard(player2, List.of(krosanVerge));
        prepareMainPhase(player1);

        harness.castFromHand(player1, trance, "{2}{R}");
        harness.passBothPriorities();
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.playGraveyardLand(player2, krosanVerge.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(krosanVerge);
        harness.assertNotOnBattlefield(player2, "Krosan Verge");
    }

    @Test
    @DisplayName("Does not allow its controller to play cards from their own graveyard")
    void doesNotAllowControllerToPlayCardsFromTheirOwnGraveyard() {
        ShamansTrance trance = new ShamansTrance();
        EmberShot emberShot = new EmberShot();
        harness.setGraveyard(player1, List.of(emberShot));
        prepareMainPhase(player1);

        harness.castFromHand(player1, trance, "{2}{R}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player1, emberShot.getId(), null, player2.getId(), List.of(), List.of(), null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(emberShot);
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
