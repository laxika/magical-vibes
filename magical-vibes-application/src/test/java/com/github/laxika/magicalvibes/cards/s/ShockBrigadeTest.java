package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ShockBrigade.class)
class ShockBrigadeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking red Warrior token")
    void attackingCreatesTappedAndAttackingWarriorToken() {
        addCreatureReady(player1, new ShockBrigade());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().isTapped()).isTrue();
        assertThat(tokens.getFirst().isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The mobilized token is sacrificed at the beginning of the next end step")
    void mobilizedTokenIsSacrificedAtNextEndStep() {
        addCreatureReady(player1, new ShockBrigade());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isOne();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new ShockBrigade());
        addCreatureReady(player2, new ShockBrigade());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new ShockBrigade());
        Permanent first = addCreatureReady(player2, new ShockBrigade());
        Permanent second = addCreatureReady(player2, new ShockBrigade());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void warriorTokenCanBeBlockedByOneCreature() {
        addCreatureReady(player1, new ShockBrigade());
        Permanent blocker = addCreatureReady(player2, new ShockBrigade());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        Permanent token = findPermanent(player1, "Warrior");
        prepareDeclareBlockers();
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, tokenIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void mobilizeResolvesAndSacrificesTokenAfterSourceLeaves() {
        Permanent source = addCreatureReady(player1, new ShockBrigade());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).isNotEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        resolveAllTriggers();
        assertThat(findPermanents(player1, "Warrior")).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).isEmpty();
    }
}
