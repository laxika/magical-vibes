package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MichelangeloMutantBFF.class, GrizzlyBears.class})
class MichelangeloMutantBFFTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Mutagen token")
    void etbCreatesMutagenToken() {
        harness.setHand(player1, List.of(new MichelangeloMutantBFF()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking creates a Mutagen token")
    void attackCreatesMutagenToken() {
        addMichelangeloReady(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("A creature with a counter cannot be blocked by more than one creature")
    void counteredCreatureCannotBeBlockedByTwoCreatures() {
        Permanent michelangelo = addMichelangeloReady(player1);
        michelangelo.setCounterCount(CounterType.STUN, 1);
        addReadyBlocker(player2);
        addReadyBlocker(player2);

        michelangelo.setAttacking(true);
        prepareBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("A creature without a counter can be blocked by two creatures")
    void uncounteredCreatureCanBeBlockedByTwoCreatures() {
        addMichelangeloReady(player1);
        addReadyBlocker(player2);
        addReadyBlocker(player2);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        prepareBlockerDeclaration();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1)
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent addMichelangeloReady(Player player) {
        Permanent michelangelo = new Permanent(new MichelangeloMutantBFF());
        michelangelo.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(michelangelo);
        return michelangelo;
    }

    private void addReadyBlocker(Player player) {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(blocker);
    }

    private void prepareBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
