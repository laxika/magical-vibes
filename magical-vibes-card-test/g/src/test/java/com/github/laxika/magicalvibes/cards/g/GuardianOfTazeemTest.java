package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.ReefShaman;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardianOfTazeem.class, Forest.class, Island.class, GrizzlyBears.class, ReefShaman.class})
class GuardianOfTazeemTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall taps an opponent's creature")
    void landfallTapsOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new GuardianOfTazeem());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        playLand(new Forest());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Island landfall makes the creature skip its next untap step")
    void islandLandfallSkipsNextUntap() {
        harness.addToBattlefieldAndReturn(player1, new GuardianOfTazeem());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        playLand(new Island());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("An already tapped creature still skips exactly its next controller untap")
    void alreadyTappedCreatureSkipsOnlyNextUntap() {
        harness.addToBattlefieldAndReturn(player1, new GuardianOfTazeem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GuardianOfTazeem());
        target.tap();
        harness.forceActivePlayer(player1);

        playLand(new Island());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A non-Island does not prevent the target's next untap")
    void nonIslandTargetUntapsNormally() {
        harness.addToBattlefieldAndReturn(player1, new GuardianOfTazeem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GuardianOfTazeem());
        harness.forceActivePlayer(player1);

        playLand(new Forest());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's land entering does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GuardianOfTazeem());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Island()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(guardian.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Forest becoming an Island in response prevents the target's next untap")
    void landBecomingIslandBeforeResolutionPreventsUntap() {
        assertLandTypeAtResolution(new Forest(), "ISLAND", true);
    }

    @Test
    @DisplayName("An Island becoming a Forest in response does not prevent untapping")
    void landLosingIslandTypeBeforeResolutionAllowsUntap() {
        assertLandTypeAtResolution(new Island(), "FOREST", false);
    }

    private void assertLandTypeAtResolution(Card landCard, String chosenType, boolean shouldStayTapped) {
        harness.addToBattlefieldAndReturn(player1, new GuardianOfTazeem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GuardianOfTazeem());
        Permanent shaman = addCreatureReady(player1, new ReefShaman());
        harness.forceActivePlayer(player1);

        playLand(landCard);
        harness.handlePermanentChosen(player1, target.getId());
        Permanent land = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == landCard)
                .findFirst().orElseThrow();
        int shamanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(shaman);
        harness.activateAbility(player1, shamanIndex, null, land.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, chosenType);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isEqualTo(shouldStayTapped);
    }
    private void playLand(Card land) {
        harness.setHand(player1, List.of(land));
        harness.playLand(player1, 0);
    }
}
