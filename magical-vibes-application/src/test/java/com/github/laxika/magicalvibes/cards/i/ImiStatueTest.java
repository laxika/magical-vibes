package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OnyxTalisman;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImiStatueTest extends BaseCardTest {

    @Test
    @DisplayName("Only the chosen artifact untaps; other artifacts stay tapped, non-artifacts untap")
    void picksOneArtifactToUntap() {
        addCreatureReady(player1, new ImiStatue());
        Permanent feather = addCreatureReady(player1, new AngelsFeather());
        Permanent talisman = addCreatureReady(player1, new OnyxTalisman());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = addCreatureReady(player1, new Forest());
        feather.tap();
        talisman.tap();
        bears.tap();
        forest.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(feather.getId()));

        assertThat(feather.isTapped()).isFalse();
        assertThat(talisman.isTapped()).isTrue();
        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Imi Statue still imposes the restriction")
    void tappedStatueStillRestricts() {
        Permanent statue = addCreatureReady(player1, new ImiStatue());
        statue.tap();
        Permanent feather = addCreatureReady(player1, new AngelsFeather());
        feather.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(feather.getId()));

        assertThat(feather.isTapped()).isFalse();
        assertThat(statue.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A single tapped artifact untaps without a choice")
    void singleArtifactUntapsNormally() {
        addCreatureReady(player1, new ImiStatue());
        Permanent feather = addCreatureReady(player1, new AngelsFeather());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        feather.tap();
        bears.tap();

        advanceToNextTurn(player2);

        assertThat(feather.isTapped()).isFalse();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Imi Statue restricts your untap step too")
    void opponentStatueRestrictsYourUntap() {
        addCreatureReady(player2, new ImiStatue());
        Permanent feather = addCreatureReady(player1, new AngelsFeather());
        Permanent talisman = addCreatureReady(player1, new OnyxTalisman());
        feather.tap();
        talisman.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(talisman.getId()));

        assertThat(talisman.isTapped()).isFalse();
        assertThat(feather.isTapped()).isTrue();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
