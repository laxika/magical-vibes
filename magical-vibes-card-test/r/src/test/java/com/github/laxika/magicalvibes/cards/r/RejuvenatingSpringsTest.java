package com.github.laxika.magicalvibes.cards.r;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(RejuvenatingSprings.class)
class RejuvenatingSpringsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped in a two-player game")
    void entersTappedInTwoPlayerGame() {
        playRejuvenatingSprings();

        assertThat(findPermanent(player1, "Rejuvenating Springs").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when its controller has two opponents")
    void entersUntappedWithTwoOpponents() {
        addThirdPlayer();

        playRejuvenatingSprings();

        assertThat(findPermanent(player1, "Rejuvenating Springs").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingForGreenProducesMana() {
        addReadyRejuvenatingSprings();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingForBlueProducesMana() {
        addReadyRejuvenatingSprings();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot produce mana while tapped after entering")
    void cannotProduceManaWhileTapped() {
        playRejuvenatingSprings();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Can immediately produce mana when entering untapped")
    void canProduceManaImmediatelyWithTwoOpponents() {
        addThirdPlayer();
        playRejuvenatingSprings();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(findPermanent(player1, "Rejuvenating Springs").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enters tapped when put onto the battlefield in a two-player game")
    void entersTappedWithoutLandPlay() {
        Permanent permanent = harness.enterBattlefieldAndReturn(player1, new RejuvenatingSprings());

        assertThat(permanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when put onto the battlefield with two opponents")
    void entersUntappedWithoutLandPlayWithTwoOpponents() {
        addThirdPlayer();

        Permanent permanent = harness.enterBattlefieldAndReturn(player1, new RejuvenatingSprings());

        assertThat(permanent.isTapped()).isFalse();
    }

    private void playRejuvenatingSprings() {
        harness.setHand(player1, List.of(new RejuvenatingSprings()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadyRejuvenatingSprings() {
        return addCreatureReady(player1, new RejuvenatingSprings());
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
