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

@CardUsed(SpireGarden.class)
class SpireGardenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped in a two-player game")
    void entersTappedInTwoPlayerGame() {
        playSpireGarden();

        assertThat(findPermanent(player1, "Spire Garden").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when its controller has two opponents")
    void entersUntappedWithTwoOpponents() {
        addThirdPlayer();

        playSpireGarden();

        assertThat(findPermanent(player1, "Spire Garden").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingForRedProducesMana() {
        addReadySpireGarden();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingForGreenProducesMana() {
        addReadySpireGarden();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can produce mana immediately after entering untapped and pays its tap cost")
    void producesManaOnTurnItEnters() {
        addThirdPlayer();
        playSpireGarden();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(findPermanent(player1, "Spire Garden").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entry replacement also applies when the land is put onto the battlefield")
    void entersTappedWhenPutOntoBattlefield() {
        Permanent permanent = harness.enterBattlefieldAndReturn(player2, new SpireGarden());

        assertThat(permanent.isTapped()).isTrue();
    }

    private void playSpireGarden() {
        harness.setHand(player1, List.of(new SpireGarden()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadySpireGarden() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new SpireGarden());
        permanent.setSummoningSick(false);
        return permanent;
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
