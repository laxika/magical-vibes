package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

@CardUsed({TurbulentWilderness.class, Forest.class})
class TurbulentWildernessTest extends BaseCardTest {

    private Player thirdPlayer;

    @Test
    @DisplayName("Enters tapped when opponents control fewer than eight lands")
    void entersTappedWithFewerThanEightOpponentLands() {
        addLands(player2, 7);

        playTurbulentWilderness();

        assertThat(findWilderness(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when opponents control eight lands")
    void entersUntappedWithEightOpponentLands() {
        addLands(player2, 8);

        playTurbulentWilderness();

        assertThat(findWilderness(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Counts lands controlled by multiple opponents together")
    void countsLandsAcrossOpponents() {
        addThirdPlayer();
        addLands(player2, 4);
        addLands(thirdPlayer, 4);

        playTurbulentWilderness();

        assertThat(findWilderness(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("The controller's lands do not satisfy the condition")
    void controllerLandsDoNotCount() {
        addLands(player1, 8);

        playTurbulentWilderness();

        assertThat(findWilderness(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingForGreenProducesMana() {
        Permanent wilderness = addReadyWilderness(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(wilderness.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingForBlueProducesMana() {
        Permanent wilderness = addReadyWilderness(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(wilderness.isTapped()).isTrue();
    }

    private void playTurbulentWilderness() {
        harness.setHand(player1, List.of(new TurbulentWilderness()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
    }

    private Permanent addReadyWilderness(Player player) {
        Permanent wilderness = new Permanent(new TurbulentWilderness());
        wilderness.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(wilderness);
        return wilderness;
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    private Permanent findWilderness(Player player) {
        return findPermanent(player, "Turbulent Wilderness");
    }

    private void addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        thirdPlayer = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, thirdPlayer.getUsername());
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection("conn-3"), thirdPlayer.getId(), thirdPlayer.getUsername());
    }
}
