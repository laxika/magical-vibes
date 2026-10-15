package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
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

@CardUsed({StoicAngel.class, CylianElf.class, Forest.class, StaticOrb.class})
class StoicAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Only the one chosen creature untaps; other creatures stay tapped, non-creatures untap normally")
    void picksOneCreatureLandsUntapFreely() {
        addCreatureReady(player1, new StoicAngel());
        Permanent bears1 = addCreatureReady(player1, new CylianElf());
        Permanent bears2 = addCreatureReady(player1, new CylianElf());
        Permanent forest = addCreatureReady(player1, new Forest());
        bears1.tap();
        bears2.tap();
        forest.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears1.getId()));

        assertThat(bears1.isTapped()).isFalse();
        assertThat(bears2.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("One tapped creature untaps normally without a choice")
    void oneCreatureUntapsNormally() {
        addCreatureReady(player1, new StoicAngel());
        Permanent bears = addCreatureReady(player1, new CylianElf());
        Permanent forest = addCreatureReady(player1, new Forest());
        bears.tap();
        forest.tap();

        advanceToNextTurn(player2);

        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Stoic Angel restricts your untap step too")
    void opponentStoicAngelRestrictsYourUntap() {
        addCreatureReady(player2, new StoicAngel());
        Permanent bears1 = addCreatureReady(player1, new CylianElf());
        Permanent bears2 = addCreatureReady(player1, new CylianElf());
        Permanent forest = addCreatureReady(player1, new Forest());
        bears1.tap();
        bears2.tap();
        forest.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears1.getId()));

        assertThat(bears1.isTapped()).isFalse();
        assertThat(bears2.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The lock applies even while Stoic Angel itself is tapped (unlike Static Orb)")
    void appliesWhileAngelTapped() {
        Permanent angel = addCreatureReady(player1, new StoicAngel());
        angel.tap();
        Permanent bears = addCreatureReady(player1, new CylianElf());
        bears.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.isTapped()).isFalse();
        assertThat(angel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The controller may choose no creatures while lands still untap")
    void mayChooseNoCreatures() {
        addCreatureReady(player1, new StoicAngel());
        Permanent first = addCreatureReady(player1, new CylianElf());
        Permanent second = addCreatureReady(player1, new CylianElf());
        Permanent forest = addCreatureReady(player1, new Forest());
        first.tap();
        second.tap();
        forest.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Multiple Angels still allow one creature to untap")
    void multipleAngelsAllowOneCreature() {
        addCreatureReady(player1, new StoicAngel());
        addCreatureReady(player2, new StoicAngel());
        Permanent first = addCreatureReady(player1, new CylianElf());
        Permanent second = addCreatureReady(player1, new CylianElf());
        first.tap();
        second.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Stoic Angel does not bypass Static Orb's limit on total permanents")
    void angelFirstStillEnforcesTotalPermanentCap() {
        addCreatureReady(player1, new StoicAngel());
        addCreatureReady(player1, new StaticOrb());
        Permanent first = addCreatureReady(player1, new CylianElf());
        Permanent second = addCreatureReady(player1, new CylianElf());
        Permanent firstForest = addCreatureReady(player1, new Forest());
        Permanent secondForest = addCreatureReady(player1, new Forest());
        first.tap();
        second.tap();
        firstForest.tap();
        secondForest.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        long untapped = List.of(first, second, firstForest, secondForest).stream()
                .filter(permanent -> !permanent.isTapped()).count();
        assertThat(untapped).isLessThanOrEqualTo(2L);
    }

    @Test
    @DisplayName("Static Orb does not bypass Stoic Angel's limit on creatures")
    void orbFirstStillEnforcesCreatureCap() {
        addCreatureReady(player1, new StaticOrb());
        addCreatureReady(player1, new StoicAngel());
        Permanent first = addCreatureReady(player1, new CylianElf());
        Permanent second = addCreatureReady(player1, new CylianElf());
        Permanent forest = addCreatureReady(player1, new Forest());
        first.tap();
        second.tap();
        forest.tap();

        advanceToNextTurn(player2);

        assertThatThrownBy(() ->
                harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(currentActivePlayer, TurnStep.CLEANUP);
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
