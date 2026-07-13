package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StaticOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Only the two chosen permanents untap; the rest stay tapped")
    void picksTwoOfThreeToUntap() {
        addReady(player1, new StaticOrb());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent forest = addReady(player1, new Forest());
        Permanent feather = addReady(player1, new AngelsFeather());
        bears.tap();
        forest.tap();
        feather.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), forest.getId()));

        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(feather.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Static Orb imposes no restriction — everything untaps normally")
    void tappedStaticOrbImposesNoRestriction() {
        Permanent orb = addReady(player1, new StaticOrb());
        orb.tap();
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent forest = addReady(player1, new Forest());
        Permanent feather = addReady(player1, new AngelsFeather());
        bears.tap();
        forest.tap();
        feather.tap();

        advanceToNextTurn(player2);

        // No choice was presented; the untap step untapped everything, including the Orb.
        assertThat(orb.isTapped()).isFalse();
        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(feather.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two or fewer permanents untap normally without a choice")
    void twoOrFewerUntapNormally() {
        addReady(player1, new StaticOrb());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent forest = addReady(player1, new Forest());
        bears.tap();
        forest.tap();

        advanceToNextTurn(player2);

        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's untapped Static Orb restricts your untap step too")
    void opponentStaticOrbRestrictsYourUntap() {
        addReady(player2, new StaticOrb());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent forest = addReady(player1, new Forest());
        Permanent feather = addReady(player1, new AngelsFeather());
        bears.tap();
        forest.tap();
        feather.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isTrue();
        assertThat(feather.isTapped()).isTrue();
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
