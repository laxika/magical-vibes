package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WinterOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Only the one chosen land untaps; other lands stay tapped, non-lands untap freely")
    void picksOneLandToUntapNonLandsUntapFreely() {
        addReady(player1, new WinterOrb());
        Permanent forest = addReady(player1, new Forest());
        Permanent mountain = addReady(player1, new Mountain());
        Permanent bears = addReady(player1, new GrizzlyBears());
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
        Permanent orb = addReady(player1, new WinterOrb());
        orb.tap();
        Permanent forest = addReady(player1, new Forest());
        Permanent mountain = addReady(player1, new Mountain());
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
        addReady(player1, new WinterOrb());
        Permanent forest = addReady(player1, new Forest());
        forest.tap();

        advanceToNextTurn(player2);

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's untapped Winter Orb restricts your land untap too")
    void opponentWinterOrbRestrictsYourUntap() {
        addReady(player2, new WinterOrb());
        Permanent forest = addReady(player1, new Forest());
        Permanent mountain = addReady(player1, new Mountain());
        forest.tap();
        mountain.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        assertThat(forest.isTapped()).isFalse();
        assertThat(mountain.isTapped()).isTrue();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
