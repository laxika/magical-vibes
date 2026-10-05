package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.InteractionAnswer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NathsElite.class, Forest.class, GrizzlyBears.class})
class NathsEliteTest extends BaseCardTest {

    private Permanent castNathsElite() {
        return castNathsElite(false);
    }

    private Permanent castNathsElite(boolean putOnBottom) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NathsElite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (ETB clash trigger placed)
        harness.passBothPriorities(); // resolve ETB clash effect

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(putOnBottom ? List.of() : List.of(0), putOnBottom ? List.of(0) : List.of()));
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(putOnBottom ? List.of() : List.of(0), putOnBottom ? List.of(0) : List.of()));

        return findPermanent(player1, "Nath's Elite");
    }

    @Test
    @DisplayName("Winning the clash puts a +1/+1 counter on Nath's Elite")
    void wonClashAddsCounter() {
        // Grizzly Bears has greater mana value than Forest.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        Permanent elite = castNathsElite();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(elite.getEffectivePower()).isEqualTo(5);
        assertThat(elite.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Losing the clash leaves Nath's Elite without a counter")
    void lostClashAddsNoCounter() {
        // Forest has lower mana value than Grizzly Bears.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        Permanent elite = castNathsElite();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(elite.getEffectivePower()).isEqualTo(4);
        assertThat(elite.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("All able creatures must block Nath's Elite")
    void allAbleCreaturesMustBlock() {
        Permanent elite = addCreatureReady(player1, new NathsElite());
        elite.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        // Both creatures are required to block.
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        // Assigning both blockers satisfies the requirement.
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tied clash does not put a counter on Nath's Elite")
    void tiedClashAddsNoCounter() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        Permanent elite = castNathsElite();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Tapped creatures are not required to block Nath's Elite")
    void tappedCreaturesNeedNotBlock() {
        Permanent elite = addCreatureReady(player1, new NathsElite());
        elite.setAttacking(true);
        Permanent tapped = addCreatureReady(player2, new GrizzlyBears());
        tapped.setTapped(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(tapped.isBlocking()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Putting the revealed cards on the bottom does not change the clash winner")
    void bottomChoicesPreserveClashOutcome() {
        NathsElite revealed = new NathsElite();
        Forest next = new Forest();
        Forest opponentRevealed = new Forest();
        NathsElite opponentNext = new NathsElite();
        harness.setLibrary(player1, List.of(revealed, next));
        harness.setLibrary(player2, List.of(opponentRevealed, opponentNext));

        Permanent elite = castNathsElite(true);

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, revealed);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentNext, opponentRevealed);
    }
}