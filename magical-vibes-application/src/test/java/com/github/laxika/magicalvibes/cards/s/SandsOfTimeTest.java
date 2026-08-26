package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Breezekeeper;
import com.github.laxika.magicalvibes.cards.f.Fervor;
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

class SandsOfTimeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped non-A/C/L permanents stay tapped through the skipped untap step")
    void skipsUntapStep() {
        addReady(player1, new SandsOfTime());
        // Enchantments are not flipped by the upkeep ability — so a tapped enchantment staying
        // tapped proves the untap step itself was skipped (advanceToNextTurn auto-resolves upkeep).
        Permanent fervor = addReady(player1, new Fervor());
        fervor.tap();

        advanceToNextTurn(player2);

        assertThat(fervor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Skipping the untap step prevents phasing")
    void skipPreventsPhasing() {
        addReady(player1, new SandsOfTime());
        Permanent keeper = addReady(player1, new Breezekeeper());

        advanceToNextTurn(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keeper);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(keeper);
    }

    @Test
    @DisplayName("Upkeep simultaneously flips tap states of artifacts, creatures, and lands")
    void upkeepFlipsTapStates() {
        Permanent sands = addReady(player1, new SandsOfTime());
        Permanent tappedBears = addReady(player1, new GrizzlyBears());
        Permanent untappedBears = addReady(player1, new GrizzlyBears());
        Permanent tappedForest = addReady(player1, new Forest());
        Permanent untappedForest = addReady(player1, new Forest());
        Permanent fervor = addReady(player1, new Fervor());
        tappedBears.tap();
        tappedForest.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(tappedBears.isTapped()).isFalse();
        assertThat(untappedBears.isTapped()).isTrue();
        assertThat(tappedForest.isTapped()).isFalse();
        assertThat(untappedForest.isTapped()).isTrue();
        assertThat(sands.isTapped()).isTrue();
        assertThat(fervor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's upkeep flips only that player's matching permanents")
    void flipsOnlyActivePlayersPermanents() {
        addReady(player1, new SandsOfTime());
        Permanent ownBears = addReady(player1, new GrizzlyBears());
        Permanent oppBears = addReady(player2, new GrizzlyBears());
        ownBears.tap();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(ownBears.isTapped()).isTrue();
        assertThat(oppBears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Still skips untap and flips while Sands of Time itself is tapped")
    void worksWhileTapped() {
        Permanent sands = addReady(player1, new SandsOfTime());
        Permanent fervor = addReady(player1, new Fervor());
        Permanent bears = addReady(player1, new GrizzlyBears());
        sands.tap();
        fervor.tap();
        bears.tap();

        advanceToNextTurn(player2);
        assertThat(fervor.isTapped()).isTrue(); // skip still applied while Sands is tapped
        assertThat(bears.isTapped()).isFalse(); // upkeep flip already resolved via auto-pass
        assertThat(sands.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
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
