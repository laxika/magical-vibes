package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.StaticOrb;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WinterOrb.class, Forest.class, GrizzlyBears.class, Mountain.class, StaticOrb.class})
class WinterOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Only the one chosen land untaps; other lands stay tapped, non-lands untap freely")
    void picksOneLandToUntapNonLandsUntapFreely() {
        addCreatureReady(player1, new WinterOrb());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent mountain = addCreatureReady(player1, new Mountain());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        forest.tap();
        mountain.tap();
        bears.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(forest.isTapped()).isFalse();
        assertThat(mountain.isTapped()).isTrue();
        // Non-land permanents are unaffected by the cap.
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Winter Orb imposes no restriction — every land untaps")
    void tappedWinterOrbImposesNoRestriction() {
        Permanent orb = addCreatureReady(player1, new WinterOrb());
        orb.tap();
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent mountain = addCreatureReady(player1, new Mountain());
        forest.tap();
        mountain.tap();

        advanceToNextTurn(player2);

        assertThat(orb.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(mountain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("One or fewer lands untap normally without a choice")
    void oneOrFewerLandsUntapNormally() {
        addCreatureReady(player1, new WinterOrb());
        Permanent forest = addCreatureReady(player1, new Forest());
        forest.tap();

        advanceToNextTurn(player2);

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The controller may choose not to untap any land")
    void mayChooseNoLandToUntap() {
        addCreatureReady(player1, new WinterOrb());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent mountain = addCreatureReady(player1, new Mountain());
        forest.tap();
        mountain.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(forest.isTapped()).isTrue();
        assertThat(mountain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's untapped Winter Orb restricts your land untap too")
    void opponentWinterOrbRestrictsYourUntap() {
        addCreatureReady(player2, new WinterOrb());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent mountain = addCreatureReady(player1, new Mountain());
        forest.tap();
        mountain.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(forest.isTapped()).isFalse();
        assertThat(mountain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Multiple Winter Orbs still allow one land to untap")
    void multipleWinterOrbsAllowOneLand() {
        addCreatureReady(player1, new WinterOrb());
        addCreatureReady(player2, new WinterOrb());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent mountain = addCreatureReady(player1, new Mountain());
        forest.tap();
        mountain.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(mountain.getId()));

        assertThat(forest.isTapped()).isTrue();
        assertThat(mountain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Lands already untapped do not consume the one-land allowance")
    void alreadyUntappedLandDoesNotConsumeAllowance() {
        addCreatureReady(player1, new WinterOrb());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent mountain = addCreatureReady(player1, new Mountain());
        mountain.tap();

        advanceToNextTurn(player2);

        assertThat(forest.isTapped()).isFalse();
        assertThat(mountain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Static Orb cannot allow two lands to untap while Winter Orb is active")
    void staticOrbDoesNotOverrideWinterOrbsLandLimit() {
        addCreatureReady(player1, new StaticOrb());
        addCreatureReady(player1, new WinterOrb());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent mountain = addCreatureReady(player1, new Mountain());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        forest.tap();
        mountain.tap();
        bears.tap();

        advanceToNextTurn(player2);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player1, List.of(forest.getId(), mountain.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(forest.isTapped()).isTrue();
        assertThat(mountain.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Winter Orb cannot bypass Static Orb's limit on all permanents")
    void winterOrbDoesNotOverrideStaticOrbsPermanentLimit() {
        addCreatureReady(player1, new WinterOrb());
        addCreatureReady(player1, new StaticOrb());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent mountain = addCreatureReady(player1, new Mountain());
        Permanent firstBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBears = addCreatureReady(player1, new GrizzlyBears());
        forest.tap();
        mountain.tap();
        firstBears.tap();
        secondBears.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(forest.isTapped()).isFalse();
        assertThat(mountain.isTapped()).isTrue();
        assertThat(List.of(forest, mountain, firstBears, secondBears).stream()
                .filter(permanent -> !permanent.isTapped()).count()).isLessThanOrEqualTo(2L);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(currentActivePlayer, TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
