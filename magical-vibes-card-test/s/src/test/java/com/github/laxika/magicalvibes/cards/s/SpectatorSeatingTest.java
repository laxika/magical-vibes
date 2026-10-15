package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectatorSeating.class})
class SpectatorSeatingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped in a two-player game")
    void entersTappedInTwoPlayerGame() {
        playSpectatorSeating();

        assertThat(findPermanent(player1, "Spectator Seating").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when its controller has two opponents")
    void entersUntappedWithTwoOpponents() {
        addThirdPlayer();

        playSpectatorSeating();

        assertThat(findPermanent(player1, "Spectator Seating").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingForRedProducesMana() {
        addReadySpectatorSeating();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingForWhiteProducesMana() {
        addReadySpectatorSeating();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters tapped when put onto the battlefield in a two-player game")
    void entersTappedWhenPutOntoBattlefield() {
        Permanent permanent = harness.enterBattlefieldAndReturn(player1, new SpectatorSeating());

        assertThat(permanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when put onto the battlefield with two opponents")
    void entersUntappedWhenPutOntoBattlefieldWithTwoOpponents() {
        addThirdPlayer();

        Permanent permanent = harness.enterBattlefieldAndReturn(player1, new SpectatorSeating());

        assertThat(permanent.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A newly played untapped land can produce mana immediately and pays its tap cost")
    void newlyPlayedLandProducesManaImmediately() {
        addThirdPlayer();
        playSpectatorSeating();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(findPermanent(player1, "Spectator Seating").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void playSpectatorSeating() {
        harness.setHand(player1, List.of(new SpectatorSeating()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadySpectatorSeating() {
        return addCreatureReady(player1, new SpectatorSeating());
    }

    private void addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
    }
}
