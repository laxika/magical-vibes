package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SecretOfBloodbending.class, GrizzlyBears.class})
class SecretOfBloodbendingTest extends BaseCardTest {

    @Test
    void withoutWaterbendControlsOpponentsNextCombatOnly() {
        prepareSpell();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingCombatControl).containsEntry(player2.getId(), player1.getId());
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof SecretOfBloodbending);

        advanceToPlayer2Combat();

        assertThat(gd.mindControlledPlayerId).isEqualTo(player2.getId());
        assertThat(gd.mindControllerPlayerId).isEqualTo(player1.getId());
        assertThat(gd.mindControlUntilEndOfCombat).isTrue();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.mindControlledPlayerId).isNull();
        assertThat(gd.mindControllerPlayerId).isNull();
    }

    @Test
    void waterbendControlsOpponentsNextTurn() {
        prepareSpell();
        List<Permanent> sources = List.of(
                addCreatureReady(player1, new GrizzlyBears()), addCreatureReady(player1, new GrizzlyBears()),
                addCreatureReady(player1, new GrizzlyBears()), addCreatureReady(player1, new GrizzlyBears()),
                addCreatureReady(player1, new GrizzlyBears()), addCreatureReady(player1, new GrizzlyBears()),
                addCreatureReady(player1, new GrizzlyBears()), addCreatureReady(player1, new GrizzlyBears()),
                addCreatureReady(player1, new GrizzlyBears()), addCreatureReady(player1, new GrizzlyBears()));

        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                sources.stream().map(Permanent::getId).toList(), List.of(), false,
                null, null, List.of(), List.of(), null, null, true);
        harness.passBothPriorities();

        assertThat(sources).allMatch(Permanent::isTapped);
        assertThat(gd.pendingTurnControl).containsEntry(player2.getId(), player1.getId());
        assertThat(gd.pendingCombatControl).isEmpty();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.mindControlledPlayerId).isEqualTo(player2.getId());
        assertThat(gd.mindControllerPlayerId).isEqualTo(player1.getId());
        assertThat(gd.mindControlUntilEndOfCombat).isFalse();
    }

    private void prepareSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SecretOfBloodbending()));
        harness.addMana(player1, ManaColor.BLUE, 4);
    }

    private void advanceToPlayer2Combat() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);
    }
}
